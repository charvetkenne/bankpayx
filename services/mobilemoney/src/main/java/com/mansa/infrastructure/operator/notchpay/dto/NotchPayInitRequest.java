package com.mansa.infrastructure.operator.notchpay.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * Payload d'initialisation de paiement NotchPay.
 *
 * Endpoint : POST https://api.notchpay.co/payments/initialize
 * Auth     : Authorization: {publicKey}
 *
 * Champs obligatoires : amount, currency, email
 * Champs optionnels   : phone, reference, description, callback, metadata
 *
 * Doc : https://developer.notchpay.co/accept-payments/collect
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record NotchPayInitRequest(

        @JsonProperty("amount")
        int amount,

        @JsonProperty("currency")
        String currency,

        @JsonProperty("email")
        String email,

        @JsonProperty("phone")
        String phone,

        @JsonProperty("reference")
        String reference,

        @JsonProperty("description")
        String description,

        @JsonProperty("callback")
        String callback,

        @JsonProperty("locked")
        Boolean locked                // true = bloque le changement de canal par le client
) {}
