package com.mansa.infrastructure.paypal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "paypal")
public class PayPalConfig {

    /** PayPal Client ID (sandbox ou production). */
    private String clientId;

    /** PayPal Client Secret (sandbox ou production). */
    private String clientSecret;

    /** Base URL : sandbox = https://api-m.sandbox.paypal.com */
    private String baseUrl = "https://api-m.sandbox.paypal.com";

    /** Nom affiché sur la page PayPal. */
    private String brandName = "BankPayX";

    /** ID du webhook PayPal (pour vérification de signature). */
    private String webhookId;
}
