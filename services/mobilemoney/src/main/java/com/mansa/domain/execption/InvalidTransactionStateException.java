package com.mansa.domain.execption;

import com.mansa.domain.valueobject.TransactionStatus;

public class InvalidTransactionStateException extends RuntimeException {

    private final TransactionStatus currentStatus;
    private final TransactionStatus targetStatus;

    public InvalidTransactionStateException(TransactionStatus currentStatus, TransactionStatus targetStatus) {
        super(String.format(
                "Invalid state transition: cannot move from %s to %s",
                currentStatus, targetStatus
        ));
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }

    public TransactionStatus getCurrentStatus() { return currentStatus; }
    public TransactionStatus getTargetStatus() { return targetStatus; }
}
