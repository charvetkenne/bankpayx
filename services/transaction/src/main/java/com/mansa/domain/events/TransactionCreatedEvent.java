package com.mansa.domain.events;


import com.mansa.domain.model.TransactionType;
import java.math.BigDecimal;
import java.util.UUID;

public record TransactionCreatedEvent(UUID transactionId, BigDecimal amount, String currency, TransactionType type, String sourceAccount, String destinationAccount) {}

