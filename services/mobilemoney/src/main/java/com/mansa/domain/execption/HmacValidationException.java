package com.mansa.domain.execption;

public class HmacValidationException extends RuntimeException {

    private final String operatorCode;

    public HmacValidationException(String operatorCode, String message) {
        super(String.format("HMAC validation failed for operator %s: %s", operatorCode, message));
        this.operatorCode = operatorCode;
    }

    public String getOperatorCode() {
        return operatorCode;
    }
}