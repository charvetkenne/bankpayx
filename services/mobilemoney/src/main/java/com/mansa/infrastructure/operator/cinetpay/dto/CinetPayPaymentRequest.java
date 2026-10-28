package com.mansa.infrastructure.operator.cinetpay.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record CinetPayPaymentRequest(
        @JsonProperty("apikey") String apiKey,
        @JsonProperty("site_id") String siteId,
        @JsonProperty("transaction_id") String transactionId,
        @JsonProperty("amount") int amount,
        @JsonProperty("currency") String currency,
        @JsonProperty("description") String description,
        @JsonProperty("return_url") String returnUrl,
        @JsonProperty("notify_url") String notifyUrl,
        @JsonProperty("customer_phone_number") String customerPhoneNumber,
        @JsonProperty("channels") String channels,
        @JsonProperty("metadata") String metadata
) {}
