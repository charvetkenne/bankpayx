package com.mansa.infrastructure.security;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
//import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mansa.infrastructure.security.filter.CorrelationIdFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Spring Security configuration.
 *
 * NOTE: For full JWT/Keycloak support add to pom.xml:
 *   spring-boot-starter-security
 *   spring-boot-starter-oauth2-resource-server
 *
 * Then uncomment the oauth2ResourceServer() block below.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorrelationIdFilter          correlationIdFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    private static final String[] PUBLIC_ENDPOINTS = {
            "/actuator/health",
            "/actuator/info",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
           "/api/webhooks/stripe",   // Vérifie que ce chemin correspond à ton controller
            "/api/webhooks/paypal",   // Ajouté pour PayPal
            "/error"
    };

     @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource("https://bankpayx.vercel.app/")))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                    // Note: hasAnyAuthority est souvent plus fiable que hasAnyRole avec Supabase 
                    // sauf si ton JwtAuthenticationConverter ajoute le préfixe "ROLE_"
                    .requestMatchers(HttpMethod.POST, "/api/v1/payments").hasAnyAuthority("PAYMENT_USER", "MERCHANT", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/v1/payments/*/capture").hasAnyAuthority("MERCHANT", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/v1/payments/*/refund").hasAnyAuthority("MERCHANT", "ADMIN")
                    .requestMatchers(HttpMethod.GET,  "/api/v1/payments/**").hasAnyAuthority("PAYMENT_USER", "MERCHANT", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/v1/paypal/create_order").hasAnyAuthority("PAYMENT_USER", "MERCHANT", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/v1/paypal/capture_order").hasAnyAuthority("PAYMENT_USER", "MERCHANT", "ADMIN")
                    .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                    // .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                    .jwt(Customizer.withDefaults())
                    .authenticationEntryPoint(authenticationEntryPoint)
            )   
            .addFilterBefore(correlationIdFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }

    // ✅ SUPPRIMÉ : Le bean jwtDecoder() hardcodé sur localhost. 
    // Spring Boot le génère automatiquement et correctement grâce à la propriété 
    // 'spring.security.oauth2.resourceserver.jwt.issuer-uri' du application.yml
     @Bean
    public Converter<Jwt, Collection<GrantedAuthority>> supabaseRoleConverter() {
        return jwt -> {
            List<GrantedAuthority> authorities = new ArrayList<>();
            
            // Lire le champ "app_metadata" du token
            Map<String, Object> appMetadata = jwt.getClaim("app_metadata");
            if (appMetadata != null && appMetadata.containsKey("roles")) {
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) appMetadata.get("roles");
                // Ajouter le préfixe "ROLE_" et mettre en majuscule (ex: "payment_user" -> "ROLE_PAYMENT_USER")
                roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())));
            } else {
                // Fallback : si pas de rôles custom, on donne un rôle USER par défaut
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            }
            return authorities;
        };
    }

     @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(supabaseRoleConverter());
        
        // Définir le nom du principal comme l'UUID de l'utilisateur (le "sub" du token)
        // C'est ce que ton JwtUtils.getCurrentUserId() va récupérer !
        converter.setPrincipalClaimName("sub");
        
        return converter;
    }
       

    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${frontend.url:https://.*\\.vercel\\.app}") String frontendUrlPattern) {
        CorsConfiguration config = new CorsConfiguration();
        // On autorise Vercel (et localhost pour les tests) au lieu de "*" pour rester compatible avec allowCredentials
        config.setAllowedOriginPatterns(List.of(frontendUrlPattern, "http://localhost:*")); 
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "x-correlation-id"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L); // Met en cache les requêtes preflight (OPTIONS) pour économiser du CPU
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
   

     @Bean
    public JwtDecoder jwtDecoder(@Value("${supabase.anon-key}") String anonKey) {
        // 1. Créer un RestTemplate personnalisé
        RestTemplate restTemplate = new RestTemplate();
        
        // 2. Ajouter un intercepteur pour injecter l'en-tête 'apikey' requis par Supabase
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("apikey", anonKey);
            request.getHeaders().add("User-Agent", "Spring-Boot-Supabase-JWT"); // Évite les blocages WAF
            return execution.execute(request, body);
        });

        // 3. Configurer le décodeur avec ce RestTemplate et l'algorithme ES256
        return NimbusJwtDecoder
                .withJwkSetUri("https://irjomcdsthgqmznubqti.supabase.co/auth/v1/.well-known/jwks.json")
                .restOperations(restTemplate) // <-- Injection du client personnalisé
                .jwsAlgorithm(SignatureAlgorithm.ES256)
                .build();
    }
}
