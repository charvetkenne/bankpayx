package com.mansa.infrastructure.security;

import lombok.RequiredArgsConstructor;
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
            "/webhooks/stripe"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        System.out.println("SecurityFilterChain initialized");
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/payments").hasAnyRole("PAYMENT_USER", "MERCHANT", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/v1/payments/*/capture").hasAnyRole("MERCHANT", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/v1/payments/*/refund").hasAnyRole("MERCHANT", "ADMIN")
                    .requestMatchers(HttpMethod.GET,  "/api/v1/payments/**").hasAnyRole("PAYMENT_USER", "MERCHANT", "ADMIN")
                    .requestMatchers(HttpMethod.POST,"/api/v1/paypal/create_order").hasAnyRole("PAYMENT_USER","MERCHANT","ADMIN")
                    .requestMatchers(HttpMethod.POST,"/api/v1/paypal/capture_order").hasAnyRole("PAYMENT_USER","MERCHANT","ADMIN")
                    .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2
                                .jwt(jwt ->{System.out.println("JWT configured");
                                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter);}) .authenticationEntryPoint(authenticationEntryPoint)
                                 
                        )   
            .addFilterBefore(correlationIdFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }


        @Bean
    public OncePerRequestFilter debugFilter() {
        return new OncePerRequestFilter() {

            @Override
            protected void doFilterInternal(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    FilterChain filterChain)
                    throws ServletException, IOException {

                System.out.println("DEBUG FILTER BEFORE");

                filterChain.doFilter(request, response);

                System.out.println("DEBUG FILTER AFTER");
            }
        };
    }
    @Bean
    public JwtDecoder jwtDecoder() {
        System.out.println("JWT DECODER CREATED");
        System.out.println("STEP 1");
          NimbusJwtDecoder decoder =
        NimbusJwtDecoder.withJwkSetUri(
            "http://localhost:8180/realms/bankpayx/protocol/openid-connect/certs"
        ).build();
        System.out.println("STEP 2");
        return decoder;
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
   
}
