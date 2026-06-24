package com.mansa.domain.event;



import java.time.Instant;

public record TokenIssuedEvent(
        String username,
        String token,
        Instant occurredAt
) {}
