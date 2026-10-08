package com.mansa.infrastructure.operator.mtn.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

public record MtnCallbackPayload(
        @JsonProperty("financialTransactionId") String financialTransactionId,
        @JsonProperty("externalId") String externalId,
        @JsonProperty("amount") String amount,
        @JsonProperty("currency") String currency,
        @JsonProperty("status") String status,
        @JsonProperty("payerMessage") String payerMessage,
        @JsonProperty("payeeNote") String payeeNote,
        @JsonProperty("reason") MtnStatusResponse.MtnFailureReason reason
) {}
