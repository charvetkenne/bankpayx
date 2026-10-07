package com.mansa.infrastructure.operator.cinetpay.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CinetPayPaymentResponse(
        @JsonProperty("code") String code,
        @JsonProperty("message") String message,
        @JsonProperty("data") CinetPayData data,
        @JsonProperty("description") String description,
        @JsonProperty("api_response_id") String apiResponseId
) {
    public record CinetPayData(
            @JsonProperty("payment_token") String paymentToken,
            @JsonProperty("payment_url") String paymentUrl
    ) {}
}
