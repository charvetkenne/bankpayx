package com.mansa.infrastructure.operator.notchpay.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriétés de configuration pour l'agrégateur NotchPay.
 *
 * Authentification NotchPay (double clé) :
 *   - publicKey  → header "Authorization"  (initiation de paiement, lecture)
 *   - privateKey → header "X-Grant"        (opérations sensibles : remboursements, webhooks)
 *
 * Base URL :
 *   - Sandbox    : https://sandbox.notchpay.co
 *   - Production : https://api.notchpay.co
 */
@ConfigurationProperties(prefix = "operators.notchpay")
public record NotchPayProperties(
        String baseUrl,
        String publicKey,
        String privateKey,
        String webhookHash,       // Hash configuré dans le dashboard NotchPay pour valider les webhooks
        String callbackUrl,       // URL de callback après paiement (redirect client)
        String notifyUrl,         // URL de notification serveur (webhook)
        String environment,       // sandbox | live
        int connectTimeoutMs,
        int readTimeoutMs
) {}