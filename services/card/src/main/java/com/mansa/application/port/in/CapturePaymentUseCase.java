package com.mansa.application.port.in;

import com.mansa.domain.model.CardPayment;

public interface CapturePaymentUseCase {

    CardPayment capture(String transactionId);
}
