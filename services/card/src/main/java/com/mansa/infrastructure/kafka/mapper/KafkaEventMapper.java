package com.mansa.infrastructure.kafka.mapper;

import com.mansa.domain.event.*;
import com.mansa.infrastructure.kafka.event.*;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventMapper {

    public PaymentAuthorizedKafkaEvent toKafka(PaymentAuthorizedEvent e) {
        return PaymentAuthorizedKafkaEvent.builder()
                .eventId(e.getEventId())
                .eventType("payment.authorized")
                .transactionId(e.getTransactionId())
                .amount(e.getAmount().getAmount())
                .currency(e.getAmount().getCurrency().name())
                .correlationId(e.getCorrelationId())
                .occurredOn(e.getOccurredOn())
                .build();
    }

    public PaymentFailedKafkaEvent toKafka(PaymentFailedEvent e) {
        return PaymentFailedKafkaEvent.builder()
                .eventId(e.getEventId())
                .eventType("payment.failed")
                .transactionId(e.getTransactionId())
                .reason(e.getReason())
                .correlationId(e.getCorrelationId())
                .occurredOn(e.getOccurredOn())
                .build();
    }

    public PaymentCapturedKafkaEvent toKafka(PaymentCapturedEvent e) {
        return PaymentCapturedKafkaEvent.builder()
                .eventId(e.getEventId())
                .eventType("payment.captured")
                .transactionId(e.getTransactionId())
                .amount(e.getAmount().getAmount())
                .currency(e.getAmount().getCurrency().name())
                .correlationId(e.getCorrelationId())
                .occurredOn(e.getOccurredOn())
                .build();
    }

    public RefundCreatedKafkaEvent toKafka(RefundCreatedEvent e) {
        return RefundCreatedKafkaEvent.builder()
                .eventId(e.getEventId())
                .eventType("payment.refunded")
                .transactionId(e.getTransactionId())
                .amount(e.getAmount().getAmount())
                .currency(e.getAmount().getCurrency().name())
                .correlationId(e.getCorrelationId())
                .occurredOn(e.getOccurredOn())
                .build();
    }

     // ── PayPal events ──────────────────────────────────────────────────────────
    public PayPalOrderCapturedKafkaEvent toKafka(PayPalOrderCapturedEvent e) {
        return PayPalOrderCapturedKafkaEvent.builder()
                .eventId(e.getEventId()).eventType("paypal.order.captured")
                .transactionId(e.getTransactionId()).paypalOrderId(e.getPaypalOrderId())
                .captureId(e.getCaptureId())
                .amount(e.getAmount().getAmount()).currency(e.getAmount().getCurrency().name())
                .correlationId(e.getCorrelationId()).occurredOn(e.getOccurredOn()).build();
    }

    public Object toKafka(PayPalOrderCreatedEvent e) {
        return java.util.Map.of(
                "eventId", e.getEventId(), "eventType", "paypal.order.created",
                "transactionId", e.getTransactionId(), "paypalOrderId", e.getPaypalOrderId(),
                "correlationId", e.getCorrelationId(), "occurredOn", e.getOccurredOn().toString()
        );
    }
}
