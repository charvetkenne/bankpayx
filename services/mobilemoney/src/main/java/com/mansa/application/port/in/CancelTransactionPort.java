package com.mansa.application.port.in;


import com.mansa.application.usecase.CancelTransactionUseCase;

public interface CancelTransactionPort {
    void cancelTransaction(CancelTransactionUseCase.CancelCommand command);
}
