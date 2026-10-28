package com.mansa.infrastructure.operator.notchpay.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Réponse à l'initialisation d'un paiement NotchPay.
 *
 * Réponse type (HTTP 201) :
 * {
 *   "status":            "Accepted",
 *   "message":           "Payment initialized",
 *   "code":              201,
 *   "transaction": {
 *     "reference":       "trx.1OFePHllHoeNSL1qfsWUV1l1",
 *     "amount":          1000,
 *     "amount_total":    1000,
 *     "fee":             0,
 *     "converted_amount":1000,
 *     "currency":        "XAF",
 *     "status":          "pending",
 *     "description":     "My first payment",
 *     "customer":        "cus.40jRRfanz7tKgZrl",
 *     "sandbox":         false,
 *     "created_at":      "2024-04-25T21:39:21.000000Z",
 *     "updated_at":      "2024-04-25T21:39:21.000000Z"
 *   },
 *   "authorization_url": "https://pay.notchpay.co/..."
 * }
 */
public record NotchPayInitResponse(

        @JsonProperty("status")
        String status,

        @JsonProperty("message")
        String message,

        @JsonProperty("code")
        Integer code,

        @JsonProperty("transaction")
        NotchPayTransaction transaction,

        @JsonProperty("authorization_url")
        String authorizationUrl
) {

    public record NotchPayTransaction(

            @JsonProperty("reference")
            String reference,

            @JsonProperty("amount")
            Integer amount,

            @JsonProperty("amount_total")
            Integer amountTotal,

            @JsonProperty("fee")
            Integer fee,

            @JsonProperty("converted_amount")
            Integer convertedAmount,

            @JsonProperty("currency")
            String currency,

            @JsonProperty("status")
            String status,

            @JsonProperty("description")
            String description,

            @JsonProperty("customer")
            String customer,

            @JsonProperty("sandbox")
            Boolean sandbox,

            @JsonProperty("geo")
            String geo,

            @JsonProperty("created_at")
            String createdAt,

            @JsonProperty("updated_at")
            String updatedAt
    ) {}
}