package com.mansa.application.port.in;

import com.mansa.application.usecase.CheckTransactionStatusUseCase;

// import com.mansa.application.usecase.CheckTransactionStatusUseCase;

public interface CheckTransactionStatusPort {
    CheckTransactionStatusUseCase.TransactionStatusResult checkStatus(String transactionId);
}