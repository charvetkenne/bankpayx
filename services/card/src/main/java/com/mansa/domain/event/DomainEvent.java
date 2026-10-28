package com.mansa.domain.event;

import java.time.Instant;
import java.util.UUID;

public abstract class DomainEvent {

    private final String  eventId;
    private final Instant occurredOn;
    private final String  correlationId;

    protected DomainEvent(String correlationId) {
        this.eventId       = UUID.randomUUID().toString();
        this.occurredOn    = Instant.now();
        this.correlationId = correlationId;
    }

    public String  getEventId()       { return eventId; }
    public Instant getOccurredOn()    { return occurredOn; }
    public String  getCorrelationId() { return correlationId; }
}
