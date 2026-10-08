package com.mansa.infrastructure.exception;

public class StripeException extends RuntimeException {
    public StripeException(String message, Throwable cause) {
        super(message, cause);
    }
}
