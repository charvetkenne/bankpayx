package com.mansa.domain.exception;

public class InsufficientFundsException extends DomainException {
    public InsufficientFundsException() {
        super("Insufficient funds for this payment");
    }
}
