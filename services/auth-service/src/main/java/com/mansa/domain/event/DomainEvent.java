package com.mansa.domain.event;



import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {

    UUID eventId();

    String eventType();

    int eventVersion();

    Instant occurredAt();
}
