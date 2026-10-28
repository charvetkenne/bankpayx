package com.mansa.infrastructure.kafka.outbox;



import com.mansa.infrastructure.persistence.entity.OutboxMessageJpaEntity;
import com.mansa.infrastructure.persistence.repository.OutboxMessageJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.messaging", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OutboxEventRelay {

    private static final int BATCH_SIZE = 50;

    private final OutboxMessageJpaRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Polls pending outbox messages every 2 seconds and publishes them to Kafka.
     * This is the relay component of the Transactional Outbox Pattern.
     * Guarantees at-least-once delivery.
     *
     * Entirely disabled (bean not created, no scheduled task registered) when
     * app.messaging.enabled=false.
     */
    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void relayPendingMessages() {
        List<OutboxMessageJpaEntity> pendingMessages = outboxRepository.findPendingMessages(BATCH_SIZE);

        if (pendingMessages.isEmpty()) {
            return;
        }

        log.debug("Relaying {} outbox messages to Kafka", pendingMessages.size());

        for (OutboxMessageJpaEntity message : pendingMessages) {
            try {
                kafkaTemplate.send(message.getTopic(), message.getAggregateId(), message.getPayload())
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                log.error("Outbox relay failed: messageId={}, error={}",
                                        message.getId(), ex.getMessage());
                                outboxRepository.markAsFailed(message.getId(), ex.getMessage());
                            } else {
                                log.debug("Outbox message published: messageId={}, topic={}, offset={}",
                                        message.getId(),
                                        result.getRecordMetadata().topic(),
                                        result.getRecordMetadata().offset());
                                outboxRepository.markAsPublished(message.getId());
                            }
                        });
            } catch (Exception e) {
                log.error("Critical outbox relay error: messageId={}, error={}",
                        message.getId(), e.getMessage());
                outboxRepository.markAsFailed(message.getId(), e.getMessage());
            }
        }
    }
}








































































































// import com.mansa.infrastructure.persistence.entity.OutboxMessageJpaEntity;
// import com.mansa.infrastructure.persistence.repository.OutboxMessageJpaRepository;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.kafka.core.KafkaTemplate;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Component;
// import org.springframework.transaction.annotation.Transactional;

// import java.util.List;

// @Slf4j
// @Component
// @RequiredArgsConstructor
// public class OutboxEventRelay {

//     private static final int BATCH_SIZE = 50;

//     private final OutboxMessageJpaRepository outboxRepository;
//     private final KafkaTemplate<String, Object> kafkaTemplate;

//     /**
//      * Polls pending outbox messages every 2 seconds and publishes them to Kafka.
//      * This is the relay component of the Transactional Outbox Pattern.
//      * Guarantees at-least-once delivery.
//      */
//     @Scheduled(fixedDelay = 2000)
//     @Transactional
//     public void relayPendingMessages() {
//         List<OutboxMessageJpaEntity> pendingMessages = outboxRepository.findPendingMessages(BATCH_SIZE);

//         if (pendingMessages.isEmpty()) {
//             return;
//         }

//         log.debug("Relaying {} outbox messages to Kafka", pendingMessages.size());

//         for (OutboxMessageJpaEntity message : pendingMessages) {
//             try {
//                 kafkaTemplate.send(message.getTopic(), message.getAggregateId(), message.getPayload())
//                         .whenComplete((result, ex) -> {
//                             if (ex != null) {
//                                 log.error("Outbox relay failed: messageId={}, error={}",
//                                         message.getId(), ex.getMessage());
//                                 outboxRepository.markAsFailed(message.getId(), ex.getMessage());
//                             } else {
//                                 log.debug("Outbox message published: messageId={}, topic={}, offset={}",
//                                         message.getId(),
//                                         result.getRecordMetadata().topic(),
//                                         result.getRecordMetadata().offset());
//                                 outboxRepository.markAsPublished(message.getId());
//                             }
//                         });
//             } catch (Exception e) {
//                 log.error("Critical outbox relay error: messageId={}, error={}",
//                         message.getId(), e.getMessage());
//                 outboxRepository.markAsFailed(message.getId(), e.getMessage());
//             }
//         }
//     }
// }
