package com.mansa.infrastructure.operator.wave.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

public record WaveCallbackPayload(
        @JsonProperty("id") String id,
        @JsonProperty("checkout_status") String checkoutStatus,
        @JsonProperty("client_reference") String clientReference,
        @JsonProperty("transaction_id") String transactionId,
        @JsonProperty("amount") String amount,
        @JsonProperty("currency") String currency
) {}
