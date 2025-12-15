package com.mansa.config;


import com.mansa.filter.JwtAuthenticationFilter;
import com.mansa.filter.LoggingFilter;
import com.mansa.filter.RateLimitingFilter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.factory.RemoveRequestHeaderGatewayFilterFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Value("${services.card-service.id:card-service}")
    private String cardServiceId;

    @Value("${services.transaction-service.id:transaction-service}")
    private String transactionServiceId;

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder,
                                      JwtAuthenticationFilter jwtFilter,
                                      RateLimitingFilter rateLimitingFilter,
                                      LoggingFilter loggingFilter) {
        return builder.routes()
                // Route vers card-service
                .route("card-service", r -> r.path("/api/cards/**")
                        .filters(f -> f.filter(loggingFilter)
                                       .filter(jwtFilter)
                                       .filter(rateLimitingFilter)
                                       .removeRequestHeader("Cookie")
                                       .rewritePath("/api/cards/(?<segment>.*)", "/${segment}"))
                        .uri("lb://" + cardServiceId))
                // Route vers transaction-service
                .route("transaction-service", r -> r.path("/api/transactions/**")
                        .filters(f -> f.filter(loggingFilter)
                                       .filter(jwtFilter)
                                       .filter(rateLimitingFilter)
                                       .removeRequestHeader("Cookie")
                                       .rewritePath("/api/transactions/(?<segment>.*)", "/${segment}"))
                        .uri("lb://" + transactionServiceId))
                // Fallback route: simple status endpoint proxy example
                .route("status", r -> r.path("/status/**")
                        .filters(f -> f.filter(loggingFilter))
                        .uri("http://httpbin.org/anything"))
                .build();
    }
}
