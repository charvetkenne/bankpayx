package com.mansa.infrastructure.operator.wave.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

public record WaveStatusResponse(
        @JsonProperty("id") String id,
        @JsonProperty("checkout_status") String checkoutStatus,
        @JsonProperty("client_reference") String clientReference,
        @JsonProperty("transaction_id") String transactionId,
        @JsonProperty("amount") String amount,
        @JsonProperty("currency") String currency,
        @JsonProperty("when_completed") String whenCompleted,
        @JsonProperty("business_name") String businessName
) {}
