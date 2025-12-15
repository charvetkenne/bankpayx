package com.mansa.filter;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Filtre simple de validation JWT.
 * Remarque: en production, utilisez "spring-boot-starter-oauth2-resource-server" et configurez JWK/issuer.
 * Ici on fait un check basique d'entête Authorization pour rendre l'exemple autonome.
 */
@Component
public class JwtAuthenticationFilter implements GatewayFilter {

    @Value("${gateway.security.require-auth:true}")
    private boolean requireAuth;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!requireAuth) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        String auth = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(auth) || !auth.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = auth.substring(7).trim();
        // Exemple trivial: token must decode as "user:xxx" in Base64. Replace par validation réelle.
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            if (!decoded.startsWith("user:")) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
            // Propager l'identité en en-tête vers les microservices
            ServerHttpRequest mutated = request.mutate()
                    .header("X-Authenticated-User", decoded.substring(5))
                    .build();
            return chain.filter(exchange.mutate().request(mutated).build());
        } catch (IllegalArgumentException e) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }
    }
}
