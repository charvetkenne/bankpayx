package com.mansa.api.response;


import com.mansa.domain.valueobject.TransactionStatus;
import java.time.Instant;

public record TransactionStatusResponse(
        String transactionId,
        TransactionStatus status,
        String operatorCode,
        String operatorReference,
        String failureReason,
        Instant createdAt,
        Instant updatedAt
) {}
