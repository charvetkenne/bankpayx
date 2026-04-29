package com.mansa.domain.event;

import java.time.Instant;

public record LoginSuccessEvent(
        String username,
        Instant occurredAt
) {}