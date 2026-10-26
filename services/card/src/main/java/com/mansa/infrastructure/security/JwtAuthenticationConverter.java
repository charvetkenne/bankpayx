package com.mansa.infrastructure.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import com.mansa.infrastructure.security.SupabaseRoleConverter;

/**
 * Converts a JWT token into Spring Security authorities.
 *
 * Activate by adding to pom.xml:
 *   spring-boot-starter-oauth2-resource-server
 *
 * Then enable the converter in SecurityConfig.oauth2ResourceServer() block.
 */





@SuppressWarnings("unused")
@Component
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final SupabaseRoleConverter supabaseRoleConverter; 

    public JwtAuthenticationConverter(SupabaseRoleConverter supabaseRoleConverter){
        this.supabaseRoleConverter = supabaseRoleConverter;
    }

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        // 1. Extraire les rôles/autorités
        Collection<GrantedAuthority> authorities = supabaseRoleConverter.convert(jwt);
        
        // 2. Créer le token d'authentification. 
        // Le 3ème argument (jwt.getSubject()) devient le "Principal Name".
        // C'est lui qui sera retourné par auth.getName() dans JwtUtils !
        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }
}







// @Slf4j
// @Component
// @RequiredArgsConstructor
// public class JwtAuthenticationConverter
//          implements Converter<Jwt, AbstractAuthenticationToken>  {

//     private final KeycloakRoleConverter keycloakRoleConverter;
//     @Override
//      public AbstractAuthenticationToken convert(@NonNull  Jwt jwt) {
//         System.out.println("JWT CONVERTER CALLED");
//         Collection<GrantedAuthority> authorities = keycloakRoleConverter.convert(jwt);
//         System.out.println("AUTHORITIES "+ authorities);
//         return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
//     }
  
// }
