package com.mansa.domain.execption;


import com.mansa.domain.valueobject.OperatorCode;

public class OperatorUnavailableException extends RuntimeException {

    private final OperatorCode operatorCode;

    public OperatorUnavailableException(OperatorCode operatorCode, String message) {
        super(String.format("Operator %s is unavailable: %s", operatorCode.getDisplayName(), message));
        this.operatorCode = operatorCode;
    }

    public OperatorUnavailableException(OperatorCode operatorCode, String message, Throwable cause) {
        super(String.format("Operator %s is unavailable: %s", operatorCode.getDisplayName(), message), cause);
        this.operatorCode = operatorCode;
    }

    public OperatorCode getOperatorCode() {
        return operatorCode;
    }
}
