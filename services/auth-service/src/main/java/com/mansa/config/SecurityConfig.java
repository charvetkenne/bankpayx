package com.mansa.config;



import java.util.List;

//import com.mansa.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.Customizer;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.authentication.ProviderManager;
// import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
// import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    // private final JwtProvider jwtProvider;
    

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
       // var jwtFilter = new JwtAuthenticationFilter(jwtProvider);

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/api/auth/**", "/actuator/**").permitAll()
                        .anyRequest().authenticated())
                //.oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults())
                  .oauth2ResourceServer(oauth -> oauth
                  .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
)  
                );
        return http.build();
    } 

                @Bean
                public JwtAuthenticationConverter jwtAuthenticationConverter() {
                        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
                        converter.setAuthorityPrefix("ROLE_");
                        converter.setAuthoritiesClaimName("realm_access.roles");

                        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
                        jwtConverter.setJwtGrantedAuthoritiesConverter(converter);
                        return jwtConverter;
                }
    
                  @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("https://your-frontend.com"));
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}

