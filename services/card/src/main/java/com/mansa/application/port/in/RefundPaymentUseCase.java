package com.mansa.application.port.in;

import com.mansa.domain.model.CardPayment;

public interface RefundPaymentUseCase {

    CardPayment refund(String transactionId);
}
