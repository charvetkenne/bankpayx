package com.mansa.domain.events;

import java.util.UUID;
public record TransactionFailedEvent(UUID transactionId, String reason) {}

