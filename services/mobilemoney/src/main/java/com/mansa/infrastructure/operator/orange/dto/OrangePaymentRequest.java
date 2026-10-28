package com.mansa.infrastructure.operator.orange.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record OrangePaymentRequest(
        @JsonProperty("merchant_key") String merchantKey,
        @JsonProperty("currency") String currency,
        @JsonProperty("order_id") String orderId,
        @JsonProperty("amount") int amount,
        @JsonProperty("return_url") String returnUrl,
        @JsonProperty("cancel_url") String cancelUrl,
        @JsonProperty("notif_url") String notifUrl,
        @JsonProperty("lang") String lang,
        @JsonProperty("reference") String reference
) {}
