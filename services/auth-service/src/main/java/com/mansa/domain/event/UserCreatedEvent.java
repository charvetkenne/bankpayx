package com.mansa.domain.event;



import java.time.Instant;
import java.util.UUID;

public record UserCreatedEvent(

        UUID eventId,
        String eventType,
        int eventVersion,
        Instant occurredAt,

        UUID userId,
        String username,
        String email

) implements DomainEvent {

    public static UserCreatedEvent of(UUID userId, String username, String email) {
        return new UserCreatedEvent(
                UUID.randomUUID(),
                "USER_CREATED",
                1,
                Instant.now(),
                userId,
                username,
                email
        );
    }
}
