package com.mansa.infrastructure.persistence.adapter;


import com.mansa.application.port.out.OutboxRepositoryPort;
import com.mansa.domain.event.DomainEvent;
import com.mansa.infrastructure.persistence.entity.OutboxMessageJpaEntity;
import com.mansa.infrastructure.persistence.repository.OutboxMessageJpaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPersistenceAdapter implements OutboxRepositoryPort {

    private final OutboxMessageJpaRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void saveOutboxMessage(DomainEvent event, String topic) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to serialize domain event: " + event.getEventType(), e
            );
        }

        OutboxMessageJpaEntity entity = OutboxMessageJpaEntity.builder()
                .id(UUID.randomUUID())
                .aggregateId(event.getAggregateId())
                .eventType(event.getEventType())
                .topic(topic)
                .payload(payload)
                .correlationId(event.getCorrelationId())
                .status("PENDING")
                .build();

        outboxRepository.save(entity);
        log.debug("Outbox message persisted: id={}, eventType={}, aggregateId={}",
                entity.getId(), event.getEventType(), event.getAggregateId());
    }

    @Override
    public void markAsPublished(String outboxMessageId) {
        outboxRepository.markAsPublished(UUID.fromString(outboxMessageId));
    }

    @Override
    public void markAsFailed(String outboxMessageId, String errorMessage) {
        outboxRepository.markAsFailed(UUID.fromString(outboxMessageId), errorMessage);
    }
}
