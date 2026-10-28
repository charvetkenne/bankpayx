package com.mansa.application.usecase;


import com.mansa.domain.valueobject.TransactionStatus;

public interface RetryTransactionUseCase {

    RetryResult retryTransaction(String transactionId);

    record RetryResult(
            String transactionId,
            TransactionStatus status,
            String operatorCode,
            int retryCount
    ) {}
}