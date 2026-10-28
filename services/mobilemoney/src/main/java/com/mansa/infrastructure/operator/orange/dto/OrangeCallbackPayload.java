package com.mansa.infrastructure.operator.orange.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

public record OrangeCallbackPayload(
        @JsonProperty("status") String status,
        @JsonProperty("txnid") String txnid,
        @JsonProperty("order_id") String orderId,
        @JsonProperty("amount") int amount,
        @JsonProperty("currency") String currency,
        @JsonProperty("message") String message
) {}