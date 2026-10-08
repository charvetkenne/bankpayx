package com.mansa.infrastructure.security;

import lombok.extern.slf4j.Slf4j;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;


import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Extracts roles from a Keycloak JWT's realm_access.roles claim.
 */
@Slf4j
@Component
public class KeycloakRoleConverter
          implements Converter<Jwt, Collection<GrantedAuthority>> {

        
         @Override
         public Collection<GrantedAuthority> convert(@SuppressWarnings("null")  Jwt jwt) {

            System.out.println("KEYCLOACK CONVERTER CALLED");
             System.out.println("SUB = " + jwt.getSubject());
             System.out.println("ROLES = " + jwt.getClaimAsMap("realm_access"));

             Map<String, Object> realmAccess = jwt.getClaim("realm_access");
             System.out.println("realm_acces "+ realmAccess); 
             if (realmAccess == null || !realmAccess.containsKey("roles"))
                 return Collections.emptyList();
        
             @SuppressWarnings("unchecked")
             List<String> roles = (List<String>) realmAccess.get("roles");
             return roles.stream()
                     .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                     .collect(Collectors.toList());
         }
        

    public Collection<GrantedAuthority> extractRoles(Map<String, Object> realmAccess) {
        if (realmAccess == null) return Collections.emptyList();
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) realmAccess.getOrDefault("roles", Collections.emptyList());
        return roles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.toUpperCase()))
                .collect(Collectors.toList());
    }
}
