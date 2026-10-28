package com.mansa.application.usecase;

public interface HandleCallbackUseCase {

    void handleCallback(CallbackCommand command);

    record CallbackCommand(
            String transactionId,
            String operatorCode,
            String operatorReference,
            String status,
            String failureCode,
            String failureMessage,
            String rawPayload,
            String hmacSignature
    ) {}
}