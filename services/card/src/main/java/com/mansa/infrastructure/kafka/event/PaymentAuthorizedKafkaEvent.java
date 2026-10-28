package com.mansa.infrastructure.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/** Transport representation of a PaymentAuthorizedEvent on Kafka. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAuthorizedKafkaEvent {
    private String     eventId;
    private String     eventType;
    private String     transactionId;
    private BigDecimal amount;
    private String     currency;
    private String     correlationId;
    private Instant    occurredOn;
}
