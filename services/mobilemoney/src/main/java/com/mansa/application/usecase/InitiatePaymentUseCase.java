package com.mansa.application.usecase;



import com.mansa.domain.valueobject.*;

import java.math.BigDecimal;

public interface InitiatePaymentUseCase {

    InitiatePaymentResult initiatePayment(InitiatePaymentCommand command);

    record InitiatePaymentCommand(
            String phoneNumber,
            BigDecimal amount,
            String currencyCode,
            String operatorCode,
            String customerId,
            String idempotencyKey,
            String correlationId
    ) {}

    record InitiatePaymentResult(
            String transactionId,
            TransactionStatus status,
            String operatorCode,
            boolean isDuplicate
    ) {}
}