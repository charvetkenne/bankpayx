package com.mansa.infrastructure.kafka.event;

import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class PayPalOrderCapturedKafkaEvent {
    private String     eventId;
    private String     eventType;        // "paypal.order.captured"
    private String     transactionId;
    private String     paypalOrderId;
    private String     captureId;
    private BigDecimal amount;
    private String     currency;
    private String     correlationId;
    private Instant    occurredOn;
}
