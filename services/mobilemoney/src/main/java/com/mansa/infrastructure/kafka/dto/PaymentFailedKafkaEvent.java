package com.mansa.infrastructure.kafka.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.Instant;

@Builder
public record PaymentFailedKafkaEvent(
        @JsonProperty("eventId") String eventId,
        @JsonProperty("eventType") String eventType,
        @JsonProperty("transactionId") String transactionId,
        @JsonProperty("operatorCode") String operatorCode,
        @JsonProperty("failureReason") String failureReason,
        @JsonProperty("failureCode") String failureCode,
        @JsonProperty("correlationId") String correlationId,
        @JsonProperty("occurredAt") Instant occurredAt
) {}
