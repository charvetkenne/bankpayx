package com.mansa.infrastructure.operator.orange.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "operators.orange")
public record OrangeProperties(
        String baseUrl,
        String clientId,
        String clientSecret,
        String merchantId,
        String callbackUrl,
        int connectTimeoutMs,
        int readTimeoutMs
) {}
