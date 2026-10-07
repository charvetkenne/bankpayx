package com.mansa.application.port.in;


import com.mansa.application.usecase.InitiatePaymentUseCase;

public interface InitiatePaymentPort {
    InitiatePaymentUseCase.InitiatePaymentResult initiatePayment(InitiatePaymentUseCase.InitiatePaymentCommand command);
}