package com.mansa.infrastructure.kafka.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record PaymentInitiatedKafkaEvent(
        @JsonProperty("eventId") String eventId,
        @JsonProperty("eventType") String eventType,
        @JsonProperty("transactionId") String transactionId,
        @JsonProperty("phoneNumber") String phoneNumber,
        @JsonProperty("amount") BigDecimal amount,
        @JsonProperty("currency") String currency,
        @JsonProperty("operatorCode") String operatorCode,
        @JsonProperty("idempotencyKey") String idempotencyKey,
        @JsonProperty("correlationId") String correlationId,
        @JsonProperty("occurredAt") Instant occurredAt
) {}
