package com.mansa.infrastructure.operator.wave.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "operators.wave")
public record WaveProperties(
        String baseUrl,
        String apiKey,
        String webhookSecret,
        String callbackUrl,
        int connectTimeoutMs,
        int readTimeoutMs
) {}
