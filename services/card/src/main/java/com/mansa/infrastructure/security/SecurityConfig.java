package com.mansa.infrastructure.security;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
//import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
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
import java.util.List;

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
            "/api/webhooks/paypal"    // Ajouté pour PayPal
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
                    .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                    .authenticationEntryPoint(authenticationEntryPoint)
            )   
            .addFilterBefore(correlationIdFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }

    // ✅ SUPPRIMÉ : Le bean jwtDecoder() hardcodé sur localhost. 
    // Spring Boot le génère automatiquement et correctement grâce à la propriété 
    // 'spring.security.oauth2.resourceserver.jwt.issuer-uri' du application.yml


       

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
   
}
