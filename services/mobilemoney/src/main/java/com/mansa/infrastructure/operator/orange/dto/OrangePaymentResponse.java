package com.mansa.infrastructure.operator.orange.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

public record OrangePaymentResponse(
        @JsonProperty("status") String status,
        @JsonProperty("message") String message,
        @JsonProperty("data") OrangePaymentData data
) {
    public record OrangePaymentData(
            @JsonProperty("id") String id,
            @JsonProperty("created_date") String createdDate,
            @JsonProperty("amount") int amount,
            @JsonProperty("currency") String currency,
            @JsonProperty("status") String status,
            @JsonProperty("payment_url") String paymentUrl
    ) {}
}
