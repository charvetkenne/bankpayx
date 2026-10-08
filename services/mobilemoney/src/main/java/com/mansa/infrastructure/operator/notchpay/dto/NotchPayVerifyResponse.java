package com.mansa.infrastructure.operator.notchpay.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Réponse à la vérification d'un paiement NotchPay.
 *
 * Endpoint : GET https://api.notchpay.co/payments/{reference}
 * Auth     : Authorization: {publicKey}
 *
 * Statuts possibles :
 *   - pending    → en attente de l'action client
 *   - processing → en cours de traitement chez l'opérateur
 *   - complete   → paiement confirmé et finalisé
 *   - failed     → paiement échoué
 *   - canceled   → paiement annulé par le client ou l'opérateur
 *   - expired    → délai de paiement dépassé
 */
public record NotchPayVerifyResponse(

        @JsonProperty("status")
        String status,

        @JsonProperty("message")
        String message,

        @JsonProperty("code")
        Integer code,

        @JsonProperty("transaction")
        NotchPayTransactionDetail transaction
) {

    public record NotchPayTransactionDetail(

            @JsonProperty("reference")
            String reference,

            @JsonProperty("amount")
            Integer amount,

            @JsonProperty("amount_total")
            Integer amountTotal,

            @JsonProperty("fee")
            Integer fee,

            @JsonProperty("currency")
            String currency,

            @JsonProperty("status")
            String status,

            @JsonProperty("description")
            String description,

            @JsonProperty("customer")
            NotchPayCustomerDetail customer,

            @JsonProperty("channel")
            String channel,           // mtn, orange, airtel, etc.

            @JsonProperty("ip_address")
            String ipAddress,

            @JsonProperty("sandbox")
            Boolean sandbox,

            @JsonProperty("complete_at")
            String completeAt,

            @JsonProperty("created_at")
            String createdAt,

            @JsonProperty("updated_at")
            String updatedAt
    ) {}

    public record NotchPayCustomerDetail(

            @JsonProperty("id")
            String id,

            @JsonProperty("name")
            String name,

            @JsonProperty("email")
            String email,

            @JsonProperty("phone")
            String phone
    ) {}
}