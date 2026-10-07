package com.mansa.application.usecase;


public interface CancelTransactionUseCase {

    void cancelTransaction(CancelCommand command);

    record CancelCommand(
            String transactionId,
            String cancellationReason,
            String requestedBy
    ) {}
}