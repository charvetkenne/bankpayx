package com.mansa.infrastructure.operator.mtn.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

public record MtnPaymentResponse(
        @JsonProperty("referenceId") String referenceId,
        @JsonProperty("status") String status,
        @JsonProperty("reason") String reason
) {}
