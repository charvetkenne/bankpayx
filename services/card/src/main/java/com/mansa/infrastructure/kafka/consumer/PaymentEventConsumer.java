package com.mansa.infrastructure.kafka.consumer;

import com.mansa.infrastructure.kafka.event.PaymentAuthorizedKafkaEvent;
import com.mansa.infrastructure.kafka.event.PaymentFailedKafkaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Listens to events from other microservices (e.g. transaction-service)
 * that card-service needs to react to.
 */
@Slf4j 
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    @KafkaListener(
        topics       = "${spring.kafka.consumer.topics.payment-events:transaction.payment.events}",
        groupId      = "${spring.kafka.consumer.group-id:card-service-group}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onPaymentAuthorized(
            @Payload(required = false) PaymentAuthorizedKafkaEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC)     String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int    partition,
            @Header(KafkaHeaders.OFFSET)             long   offset) {

        if (event == null) return;
        log.info("Consumed PaymentAuthorized: txId={} correlationId={} topic={} partition={} offset={}",
                event.getTransactionId(), event.getCorrelationId(), topic, partition, offset);
        // Implement downstream logic here (e.g. notify fraud service)
    }

    @KafkaListener(
        topics       = "${spring.kafka.consumer.topics.payment-failed:transaction.payment.failed}",
        groupId      = "${spring.kafka.consumer.group-id:card-service-group}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onPaymentFailed(
            @Payload(required = false) PaymentFailedKafkaEvent event) {

        if (event == null) return;
        log.warn("Consumed PaymentFailed: txId={} reason={}",
                event.getTransactionId(), event.getReason());
    }
}
