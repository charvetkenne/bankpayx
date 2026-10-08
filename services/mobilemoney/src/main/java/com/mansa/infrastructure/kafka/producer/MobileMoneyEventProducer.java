package com.mansa.infrastructure.kafka.producer;


import com.mansa.application.port.out.EventPublisherPort;
import com.mansa.domain.event.*;
import com.mansa.infrastructure.kafka.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class MobileMoneyEventProducer implements EventPublisherPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publish(DomainEvent event, String topic) {
        Object kafkaEvent = toKafkaEvent(event);
        String key = event.getAggregateId();

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(topic, key, kafkaEvent);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event to Kafka: eventType={}, txId={}, error={}",
                        event.getEventType(), event.getAggregateId(), ex.getMessage());
            } else {
                log.info("Event published to Kafka: topic={}, partition={}, offset={}, eventType={}, txId={}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        event.getEventType(),
                        event.getAggregateId());
            }
        });
    }

    private Object toKafkaEvent(DomainEvent event) {
        if (event instanceof PaymentInitiatedEvent e) {
            return PaymentInitiatedKafkaEvent.builder()
                    .eventId(e.getEventId())
                    .eventType(e.getEventType())
                    .transactionId(e.getTransactionId())
                    .phoneNumber(e.getPhoneNumber())
                    .amount(e.getAmount())
                    .currency(e.getCurrency())
                    .operatorCode(e.getOperatorCode())
                    .idempotencyKey(e.getIdempotencyKey())
                    .correlationId(e.getCorrelationId())
                    .occurredAt(e.getOccurredAt())
                    .build();
        } else if (event instanceof PaymentSucceededEvent e) {
            return PaymentSucceededKafkaEvent.builder()
                    .eventId(e.getEventId())
                    .eventType(e.getEventType())
                    .transactionId(e.getTransactionId())
                    .operatorReference(e.getOperatorReference())
                    .amount(e.getAmount())
                    .currency(e.getCurrency())
                    .operatorCode(e.getOperatorCode())
                    .correlationId(e.getCorrelationId())
                    .succeededAt(e.getSucceededAt())
                    .occurredAt(e.getOccurredAt())
                    .build();
        } else if (event instanceof PaymentFailedEvent e) {
            return PaymentFailedKafkaEvent.builder()
                    .eventId(e.getEventId())
                    .eventType(e.getEventType())
                    .transactionId(e.getTransactionId())
                    .operatorCode(e.getOperatorCode())
                    .failureReason(e.getFailureReason())
                    .failureCode(e.getFailureCode())
                    .correlationId(e.getCorrelationId())
                    .occurredAt(e.getOccurredAt())
                    .build();
        } else if (event instanceof PaymentCancelledEvent e) {
            return PaymentCancelledKafkaEvent.builder()
                    .eventId(e.getEventId())
                    .eventType(e.getEventType())
                    .transactionId(e.getTransactionId())
                    .operatorCode(e.getOperatorCode())
                    .cancellationReason(e.getCancellationReason())
                    .correlationId(e.getCorrelationId())
                    .occurredAt(e.getOccurredAt())
                    .build();
        } else {
            throw new IllegalArgumentException(
                    "Unknown domain event type: " + event.getClass().getName());
        }
    }
}

// public class MobileMoneyEventProducer implements EventPublisherPort {

//     private final KafkaTemplate<String, Object> kafkaTemplate;

//     @Override
//     public void publish(DomainEvent event, String topic) {
//         Object kafkaEvent = toKafkaEvent(event);
//         String key = event.getAggregateId();

//         CompletableFuture<SendResult<String, Object>> future =
//                 kafkaTemplate.send(topic, key, kafkaEvent);

//         future.whenComplete((result, ex) -> {
//             if (ex != null) {
//                 log.error("Failed to publish event to Kafka: eventType={}, txId={}, error={}",
//                         event.getEventType(), event.getAggregateId(), ex.getMessage());
//             } else {
//                 log.info("Event published to Kafka: topic={}, partition={}, offset={}, eventType={}, txId={}",
//                         result.getRecordMetadata().topic(),
//                         result.getRecordMetadata().partition(),
//                         result.getRecordMetadata().offset(),
//                         event.getEventType(),
//                         event.getAggregateId());
//             }
//         });
//     }

//     private Object toKafkaEvent(DomainEvent event) {
//         return switch (event) {
//             case PaymentInitiatedEvent e -> PaymentInitiatedKafkaEvent.builder()
//                     .eventId(e.getEventId())
//                     .eventType(e.getEventType())
//                     .transactionId(e.getTransactionId())
//                     .phoneNumber(e.getPhoneNumber())
//                     .amount(e.getAmount())
//                     .currency(e.getCurrency())
//                     .operatorCode(e.getOperatorCode())
//                     .idempotencyKey(e.getIdempotencyKey())
//                     .correlationId(e.getCorrelationId())
//                     .occurredAt(e.getOccurredAt())
//                     .build();

//             case PaymentSucceededEvent e -> PaymentSucceededKafkaEvent.builder()
//                     .eventId(e.getEventId())
//                     .eventType(e.getEventType())
//                     .transactionId(e.getTransactionId())
//                     .operatorReference(e.getOperatorReference())
//                     .amount(e.getAmount())
//                     .currency(e.getCurrency())
//                     .operatorCode(e.getOperatorCode())
//                     .correlationId(e.getCorrelationId())
//                     .succeededAt(e.getSucceededAt())
//                     .occurredAt(e.getOccurredAt())
//                     .build();

//             case PaymentFailedEvent e -> PaymentFailedKafkaEvent.builder()
//                     .eventId(e.getEventId())
//                     .eventType(e.getEventType())
//                     .transactionId(e.getTransactionId())
//                     .operatorCode(e.getOperatorCode())
//                     .failureReason(e.getFailureReason())
//                     .failureCode(e.getFailureCode())
//                     .correlationId(e.getCorrelationId())
//                     .occurredAt(e.getOccurredAt())
//                     .build();

//             case PaymentCancelledEvent e -> PaymentCancelledKafkaEvent.builder()
//                     .eventId(e.getEventId())
//                     .eventType(e.getEventType())
//                     .transactionId(e.getTransactionId())
//                     .operatorCode(e.getOperatorCode())
//                     .cancellationReason(e.getCancellationReason())
//                     .correlationId(e.getCorrelationId())
//                     .occurredAt(e.getOccurredAt())
//                     .build();
//         };
//     }
// }