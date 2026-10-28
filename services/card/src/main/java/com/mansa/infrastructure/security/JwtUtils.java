package com.mansa.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtUtils {

    /**
     * Renvoie l'UUID de l'utilisateur Supabase.
     * C'est CETTE valeur qu'il faut enregistrer dans ta table 'transactions' (colonne user_id).
     */
    public String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "anonymous";
        }
        return auth.getName(); // Grâce au converter, ceci est l'UUID Supabase (le "sub")
    }

    /**
     * Vérifie si l'utilisateur a un rôle spécifique.
     * Attention : le converter ajoute automatiquement le préfixe "ROLE_".
     * Donc hasRole("ADMIN") cherchera "ROLE_ADMIN".
     */

   public boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        
        String expectedAuthority = "ROLE_" + role.toUpperCase();
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(expectedAuthority));
    }
}
