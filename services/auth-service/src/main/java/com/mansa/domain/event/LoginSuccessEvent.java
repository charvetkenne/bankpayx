package com.mansa.domain.event;

import java.time.Instant;
import java.util.UUID;

public record LoginSuccessEvent(
         UUID eventId,
        String eventType,
        int eventVersion,
        Instant occurredAt,
        String username
)implements DomainEvent {

    public static LoginSuccessEvent of(String username) {
        return new LoginSuccessEvent(
                UUID.randomUUID(),
                "LOGIN_SUCCESS",
                1,
                Instant.now(),
                username
        );
    }
}