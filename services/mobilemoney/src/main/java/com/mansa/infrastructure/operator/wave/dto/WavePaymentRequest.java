package com.mansa.infrastructure.operator.wave.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record WavePaymentRequest(
        @JsonProperty("currency") String currency,
        @JsonProperty("amount") String amount,
        @JsonProperty("error_url") String errorUrl,
        @JsonProperty("success_url") String successUrl,
        @JsonProperty("client_reference") String clientReference,
        @JsonProperty("") String id
) {}