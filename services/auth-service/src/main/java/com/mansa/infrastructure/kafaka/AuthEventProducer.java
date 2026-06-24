package com.mansa.infrastructure.kafaka;


//import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
//@RequiredArgsConstructor
public class AuthEventProducer {
    
    @Autowired 
    private final KafkaTemplate<String, Object> kafkaTemplate ;

    public AuthEventProducer( KafkaTemplate<String, Object> kafka) {
        this.kafkaTemplate = kafka;
    }
    
    public void send(String topic, Object event) {
        kafkaTemplate.send(topic, event);
    }
}