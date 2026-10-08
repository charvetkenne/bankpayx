package com.mansa.outbox;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.PageRequest;

import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class OutboxPublisher {

    private final OutboxEventRepository repository;
    private final KafkaTemplate<String,String> kafkaTemplate;

    private final int batchSize = 20;
    private final int maxAttempts = 5;

    public OutboxPublisher(OutboxEventRepository repository, KafkaTemplate<String, String> kafkaTemplate) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @SuppressWarnings("null")
    @Transactional
    public void flushPendingBatch(int size) {
        List<OutboxEvent> events =
                repository.findAndLockNextPending(PageRequest.of(0, size));

        for (OutboxEvent ev : events) {
            try {
                ev.setStatus(OutboxEventStatus.SENDING);
                ev.setLastAttempt(Instant.now());
                ev.setAttemptCount(ev.getAttemptCount() + 1);
                repository.save(ev);

                kafkaTemplate.send(ev.getTopic(), ev.getPartitionKey(), ev.getPayload()).get();

                ev.setStatus(OutboxEventStatus.SENT);
                repository.save(ev);
            } catch (Exception ex) {
                if (ev.getAttemptCount() >= maxAttempts) {
                    ev.setStatus(OutboxEventStatus.FAILED);
                } else {
                    ev.setStatus(OutboxEventStatus.PENDING);
                }
                ev.setLastAttempt(Instant.now());
                repository.save(ev);
            }
        }
    }

    @Scheduled(fixedDelayString = "${outbox.publisher.delay:2000}")
    public void scheduledFlush() {
        flushPendingBatch(batchSize);
    }
}
