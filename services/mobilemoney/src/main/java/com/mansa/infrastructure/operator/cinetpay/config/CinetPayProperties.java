package com.mansa.infrastructure.operator.cinetpay.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "operators.cinetpay")
public record CinetPayProperties(
        String baseUrl,
        String apiKey,
        String siteId,
        String secretKey,
        String callbackUrl,
        String returnUrl,
        int connectTimeoutMs,
        int readTimeoutMs
) {}
