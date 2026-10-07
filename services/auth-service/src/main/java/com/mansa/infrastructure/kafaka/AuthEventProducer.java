package com.mansa.infrastructure.kafaka;


import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.mansa.domain.event.DomainEvent;

import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class AuthEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "auth.events";

    public void send(DomainEvent event) {
        kafkaTemplate.send(TOPIC, event.eventType(), event);
    }
}