package com.mansa.domain.event;



import java.time.Instant;
import java.util.UUID;

public record TokenIssuedEvent(
          UUID eventId,
        String eventType,
        int eventVersion,
        Instant occurredAt,
        String username,
        String token
       
) implements DomainEvent {

    public static TokenIssuedEvent of(String username, String token) {
        return new TokenIssuedEvent(
                UUID.randomUUID(),
                "TOKEN_ISSUED",
                1,
                Instant.now(),
                username,
                token
        );
    }
}
