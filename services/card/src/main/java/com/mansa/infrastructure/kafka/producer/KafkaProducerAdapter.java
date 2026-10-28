package com.mansa.infrastructure.kafka.producer;

import com.mansa.application.port.out.EventPublisherPort;
import com.mansa.domain.event.*;
import com.mansa.infrastructure.exception.KafkaPublishException;
//import com.mansa.infrastructure.kafka.event.*;
import com.mansa.infrastructure.kafka.mapper.KafkaEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProducerAdapter implements EventPublisherPort {

    private static final String TOPIC_CARD_EVENTS   = "card.payment.events";
    private static final String TOPIC_PAYPAL_EVENTS = "card.paypal.events";

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaEventMapper              mapper;

    @Override
    public void publish(DomainEvent event) {
        String topic      = resolveTopic(event);
        Object kafkaEvent = toKafkaEvent(event);
        String key        = event.getCorrelationId() != null ? event.getCorrelationId() : event.getEventId();

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, kafkaEvent);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("[{}] Failed to publish {} to Kafka: {}",
                        event.getCorrelationId(), event.getClass().getSimpleName(), ex.getMessage());
            } else {
                log.info("[{}] Published {} → topic={} partition={} offset={}",
                        event.getCorrelationId(), event.getClass().getSimpleName(), topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }

    private String resolveTopic(DomainEvent event) {
        // return switch (event) {
        //     case PayPalOrderCreatedEvent  e -> TOPIC_PAYPAL_EVENTS;
        //     case PayPalOrderCapturedEvent e -> TOPIC_PAYPAL_EVENTS;
        //     default                        -> TOPIC_CARD_EVENTS;
        // };
        if (event instanceof PayPalOrderCreatedEvent) return TOPIC_CARD_EVENTS;
        if (event instanceof PayPalOrderCapturedEvent)return TOPIC_PAYPAL_EVENTS;
        else{ return TOPIC_CARD_EVENTS;}
    }

    // private Object toKafkaEvent(DomainEvent event) {
    //     return switch (event) {
    //         case PaymentAuthorizedEvent   e -> mapper.toKafka(e);
    //         case PaymentFailedEvent       e -> mapper.toKafka(e);
    //         case PaymentCapturedEvent     e -> mapper.toKafka(e);
    //         case RefundCreatedEvent       e -> mapper.toKafka(e);
    //         case PayPalOrderCapturedEvent e -> mapper.toKafka(e);
    //         case PayPalOrderCreatedEvent  e -> mapper.toKafka(e);
    //         default -> throw new KafkaPublishException("Unknown event: " + event.getClass().getName(), null);
    //     };
    //}
    private Object toKafkaEvent(DomainEvent event){
        if (event instanceof PaymentAuthorizedEvent)  return mapper.toKafka((PaymentAuthorizedEvent) event);
        if (event instanceof PaymentAuthorizedEvent)  return mapper.toKafka((PaymentAuthorizedEvent) event);
        if (event instanceof PaymentFailedEvent)      return mapper.toKafka((PaymentFailedEvent) event);
        if (event instanceof PaymentCapturedEvent)    return mapper.toKafka((PaymentCapturedEvent) event);
        if (event instanceof RefundCreatedEvent)      return mapper.toKafka((RefundCreatedEvent) event);
        if (event instanceof PayPalOrderCapturedEvent) return mapper.toKafka((PayPalOrderCapturedEvent) event);
        if (event instanceof PayPalOrderCreatedEvent) return mapper.toKafka((PaymentAuthorizedEvent) event);
       throw new KafkaPublishException("Unknown domain event type: " + (event != null ? event.getClass().getName() : "null"), null);
    }
    //private String resolveKey(DomainEvent event) {
        // Partition by correlation ID to preserve ordering per request chain
       // return event.getCorrelationId() != null ? event.getCorrelationId() : event.getEventId();
   // }
}
