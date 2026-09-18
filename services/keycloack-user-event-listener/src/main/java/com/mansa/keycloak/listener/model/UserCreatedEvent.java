package com.mansa.keycloak.listener.model;


import java.time.Instant;
import java.util.UUID;

public record UserCreatedEvent(
        UUID eventId,
        String eventType,
        int eventVersion,
        Instant occurredAt,
        String userId,
        String username,
        String email
) {
    public static UserCreatedEvent of(String userId, String username, String email) {
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