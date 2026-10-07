package com.mansa.domain.exception;

public class PaymentNotFoundException extends DomainException {
    public PaymentNotFoundException(String transactionId) {
        super("Payment not found: " + transactionId);
    }
}
