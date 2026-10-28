package com.mansa.domain.event;


import java.time.Instant;
import java.util.UUID;

public abstract sealed class DomainEvent
        permits PaymentInitiatedEvent,
                PaymentSucceededEvent,
                PaymentFailedEvent,
                PaymentCancelledEvent {

    private final String eventId;
    private final Instant occurredAt;
    private final String correlationId;

    protected DomainEvent(String correlationId) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = Instant.now();
        this.correlationId = correlationId;
    }

    public String getEventId() {
        return eventId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public abstract String getEventType();

    public abstract String getAggregateId();
}
