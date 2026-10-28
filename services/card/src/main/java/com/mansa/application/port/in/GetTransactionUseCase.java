package com.mansa.application.port.in;

import com.mansa.domain.model.CardPayment;

public interface GetTransactionUseCase {

    CardPayment getByTransactionId(String transactionId);
}
