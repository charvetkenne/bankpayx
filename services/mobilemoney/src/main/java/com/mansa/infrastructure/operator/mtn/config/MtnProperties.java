package com.mansa.infrastructure.operator.mtn.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.stereotype.Component;

import jakarta.ws.rs.DefaultValue;

@ConfigurationProperties(prefix = "operators.mtn")
public record MtnProperties(
        String baseUrl,
        String apiKey,
        String apiSecret,
        String subscriptionKey,
        String callbackUrl,
        String environment,
       @DefaultValue("5000") int connectTimeoutMs,
       @DefaultValue("30000")int readTimeoutMs,
       @DefaultValue("false") boolean subTokenEnabled
) {}