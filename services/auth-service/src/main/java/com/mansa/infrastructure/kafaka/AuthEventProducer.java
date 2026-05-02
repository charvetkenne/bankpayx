package com.mansa.infrastructure.kafaka;




//import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

//import com.mansa.domain.event.DomainEvent;

@Component
//@RequiredArgsConstructor
public class AuthEventProducer {
    
    @Autowired 
    private final KafkaTemplate<String, Object> kafkaTemplate ;
     private static final String TOPIC = "auth.events";

    public AuthEventProducer( KafkaTemplate<String, Object> kafka) {
        this.kafkaTemplate = kafka;
    }
    
    @SuppressWarnings("null")
    public void send(String type, Object payload) {
        kafkaTemplate.send(TOPIC, type , payload);
    }
} 