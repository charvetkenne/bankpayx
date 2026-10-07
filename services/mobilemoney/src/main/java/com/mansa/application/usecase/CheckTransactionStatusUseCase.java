package com.mansa.application.usecase;


import java.time.Instant;

import com.mansa.domain.valueobject.TransactionStatus;

public interface CheckTransactionStatusUseCase {

    TransactionStatusResult checkStatus(String transactionId);

    record TransactionStatusResult(
            String transactionId,
            TransactionStatus status,
            String operatorCode,
            String operatorReference,
            String failureReason,
            Instant createdAt,
            Instant updatedAt
    ) {}
}