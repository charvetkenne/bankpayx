package com.mansa.infrastructure.kafka.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.Instant;

@Builder
public record PaymentCancelledKafkaEvent(
        @JsonProperty("eventId") String eventId,
        @JsonProperty("eventType") String eventType,
        @JsonProperty("transactionId") String transactionId,
        @JsonProperty("operatorCode") String operatorCode,
        @JsonProperty("cancellationReason") String cancellationReason,
        @JsonProperty("correlationId") String correlationId,
        @JsonProperty("occurredAt") Instant occurredAt
) {}
