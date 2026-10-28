package com.mansa.infrastructure.exception;

public class PayPalException extends RuntimeException {
    public PayPalException(String message, Throwable cause) {
        super(message, cause);
    }
}
