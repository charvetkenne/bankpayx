package com.mansa.config;


import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.Claims;

import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @SuppressWarnings("null")
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, jakarta.servlet.ServletException {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                Claims claims = jwtProvider.parseToken(token).getPayload();
                String username = claims.getSubject();
                String role = claims.get("role", String.class);
                var authToken = new UsernamePasswordAuthenticationToken(
                        username, null, Collections.singleton(() -> role)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (Exception ignored) {
                // invalid token -> proceed unauthenticated
            }
        }
        chain.doFilter(request, response);
    }
}
