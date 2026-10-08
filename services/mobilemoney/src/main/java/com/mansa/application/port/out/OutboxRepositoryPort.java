package com.mansa.application.port.out;

import com.mansa.domain.event.DomainEvent;

public interface OutboxRepositoryPort {

    void saveOutboxMessage(DomainEvent event, String topic);

    void markAsPublished(String outboxMessageId);

    void markAsFailed(String outboxMessageId, String errorMessage);
}