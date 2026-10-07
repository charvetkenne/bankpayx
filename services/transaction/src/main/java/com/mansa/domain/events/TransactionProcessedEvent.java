package com.mansa.domain.events;

import java.util.UUID;
public record TransactionProcessedEvent(UUID transactionId, String status) {}
