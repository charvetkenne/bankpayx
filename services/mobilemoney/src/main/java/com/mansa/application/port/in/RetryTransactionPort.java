package com.mansa.application.port.in;

import com.mansa.application.usecase.RetryTransactionUseCase;

// import com.mansa.application.usecase.RetryTransactionUseCase;

public interface RetryTransactionPort {
    RetryTransactionUseCase.RetryResult retryTransaction(String transactionId);
}
