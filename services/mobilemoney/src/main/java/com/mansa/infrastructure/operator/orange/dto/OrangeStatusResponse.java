package com.mansa.infrastructure.operator.orange.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

public record OrangeStatusResponse(
        @JsonProperty("status") String status,
        @JsonProperty("message") String message,
        @JsonProperty("data") OrangeStatusData data
) {
    public record OrangeStatusData(
            @JsonProperty("id") String id,
            @JsonProperty("status") String status,
            @JsonProperty("txnid") String txnid,
            @JsonProperty("amount") int amount,
            @JsonProperty("currency") String currency,
            @JsonProperty("message") String message
    ) {}
}
