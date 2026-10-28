package com.mansa.infrastructure.operator.notchpay.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Payload des webhooks NotchPay.
 *
 * NotchPay envoie les notifications via POST sur l'URL configurée dans le dashboard.
 * Header de signature : "x-notch-signature" (HMAC-SHA256 du payload brut)
 *
 * Événements possibles :
 *   - payment.complete  → paiement finalisé avec succès
 *   - payment.failed    → paiement échoué
 *   - payment.canceled  → paiement annulé
 *   - payment.expired   → délai expiré
 *   - payment.pending   → paiement en attente
 *
 * Doc : https://developer.notchpay.co/get-started/webhooks
 */
public record NotchPayWebhookPayload(

        @JsonProperty("event")
        String event,

        @JsonProperty("data")
        NotchPayWebhookData data
) {

    public record NotchPayWebhookData(

            @JsonProperty("reference")
            String reference,

            @JsonProperty("amount")
            Integer amount,

            @JsonProperty("currency")
            String currency,

            @JsonProperty("status")
            String status,

            @JsonProperty("description")
            String description,

            @JsonProperty("channel")
            String channel,

            @JsonProperty("customer")
            NotchPayVerifyResponse.NotchPayCustomerDetail customer,

            @JsonProperty("complete_at")
            String completeAt,

            @JsonProperty("created_at")
            String createdAt
    ) {}

    /**
     * Convertit l'événement NotchPay en statut compréhensible par le domaine BankPayX.
     */
    public String toDomainStatus() {
        if (event == null) return "UNKNOWN";
        return switch (event) {
            case "payment.complete"  -> "SUCCESS";
            case "payment.failed"    -> "FAILED";
            case "payment.canceled"  -> "CANCELLED";
            case "payment.expired"   -> "EXPIRED";
            default                  -> "UNKNOWN";
        };
    }

    public String toFailureCode() {
        return switch (event != null ? event : "") {
            case "payment.failed"   -> "NOTCHPAY_FAILED";
            case "payment.canceled" -> "NOTCHPAY_CANCELLED";
            case "payment.expired"  -> "NOTCHPAY_EXPIRED";
            default                 -> null;
        };
    }
}