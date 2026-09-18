package com.mansa.infrastructure.kafaka;



import com.mansa.domain.event.UserCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DeadLetterConsumer {

    @KafkaListener(topics = "auth.user.created.v1.DLQ")
    public void handleDLQ(UserCreatedEvent event) {
        log.error("DLQ EVENT FAILED: {}", event);
    }
}