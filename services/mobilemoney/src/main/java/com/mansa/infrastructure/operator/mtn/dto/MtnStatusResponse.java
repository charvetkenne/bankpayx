package com.mansa.infrastructure.operator.mtn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MtnStatusResponse(
        @JsonProperty("amount") String amount,
        @JsonProperty("currency") String currency,
        @JsonProperty("externalId") String externalId,
        @JsonProperty("payer") Object payer,
        @JsonProperty("status") String status,
        @JsonProperty("reason") MtnFailureReason reason,
        @JsonProperty("financialTransactionId") String financialTransactionId
) {
    public record MtnFailureReason(
            @JsonProperty("code") String code,
            @JsonProperty("message") String message
    ) {}
}