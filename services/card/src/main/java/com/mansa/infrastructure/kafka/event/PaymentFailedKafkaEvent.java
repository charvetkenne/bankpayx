package com.mansa.infrastructure.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailedKafkaEvent {
    private String  eventId;
    private String  eventType;
    private String  transactionId;
    private String  reason;
    private String  correlationId;
    private Instant occurredOn;
}
