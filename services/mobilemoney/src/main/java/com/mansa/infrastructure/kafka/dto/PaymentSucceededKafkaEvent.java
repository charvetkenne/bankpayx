package com.mansa.infrastructure.kafka.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record PaymentSucceededKafkaEvent(
        @JsonProperty("eventId") String eventId,
        @JsonProperty("eventType") String eventType,
        @JsonProperty("transactionId") String transactionId,
        @JsonProperty("operatorReference") String operatorReference,
        @JsonProperty("amount") BigDecimal amount,
        @JsonProperty("currency") String currency,
        @JsonProperty("operatorCode") String operatorCode,
        @JsonProperty("correlationId") String correlationId,
        @JsonProperty("succeededAt") Instant succeededAt,
        @JsonProperty("occurredAt") Instant occurredAt
) {}