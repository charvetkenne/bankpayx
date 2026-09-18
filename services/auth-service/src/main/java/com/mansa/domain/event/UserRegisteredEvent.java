package com.mansa.domain.event;

import java.time.Instant;
import java.util.UUID;


public record UserRegisteredEvent(
         UUID eventId,
        String eventType,
        int eventVersion,
        Instant occurredAt,

        UUID userId,
        String username,
        String email
       
)  implements DomainEvent {

    public static UserRegisteredEvent of(UUID userId, String username, String email) {
        return new UserRegisteredEvent(
                UUID.randomUUID(),
                "USER_REGISTERED",
                1,
                Instant.now(),
                userId,
                username,
                email
        );
    }
}
