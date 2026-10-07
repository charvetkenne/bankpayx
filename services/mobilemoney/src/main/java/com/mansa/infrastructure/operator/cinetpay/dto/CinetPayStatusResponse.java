package com.mansa.infrastructure.operator.cinetpay.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

public record CinetPayStatusResponse(
        @JsonProperty("code") String code,
        @JsonProperty("message") String message,
        @JsonProperty("data") CinetPayStatusData data,
        @JsonProperty("api_response_id") String apiResponseId
) {
    public record CinetPayStatusData(
            @JsonProperty("amount") int amount,
            @JsonProperty("currency") String currency,
            @JsonProperty("status") String status,
            @JsonProperty("payment_method") String paymentMethod,
            @JsonProperty("description") String description,
            @JsonProperty("metadata") String metadata,
            @JsonProperty("operator_id") String operatorId,
            @JsonProperty("payment_date") String paymentDate
    ) {}
}
