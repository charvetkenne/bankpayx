package com.mansa.infrastructure.security;

 

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class SupabaseRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // 1. Essayer de lire les rôles personnalisés dans app_metadata (Standard Supabase)
        Map<String, Object> appMetadata = jwt.getClaim("app_metadata");
        if (appMetadata != null && appMetadata.containsKey("roles")) {
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) appMetadata.get("roles");
            roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())));
        } 
        // 2. Fallback : Si pas de rôles custom, on vérifie le claim "role" de base de Supabase
        else {
            String defaultRole = jwt.getClaimAsString("role");
            if ("authenticated".equals(defaultRole)) {
                // On donne un rôle par défaut à tout utilisateur connecté validement
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            }
        }

        // Log discret (niveau DEBUG) pour ne pas saturer le CPU en production
        log.debug("Supabase JWT converted for user {}. Authorities: {}", jwt.getSubject(), authorities);
        
        return authorities;
    }
}