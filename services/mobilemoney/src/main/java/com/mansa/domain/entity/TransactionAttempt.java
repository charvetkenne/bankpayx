package com.mansa.domain.entity;


import com.mansa.domain.valueobject.OperatorCode;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class TransactionAttempt {

    private final UUID id;
    private final UUID transactionId;
    private final OperatorCode operatorCode;
    private final int attemptNumber;
    private final Instant attemptedAt;
    private String resultCode;
    private String resultMessage;
    private boolean successful;

    public TransactionAttempt(
            UUID transactionId,
            OperatorCode operatorCode,
            int attemptNumber) {
        this.id = UUID.randomUUID();
        this.transactionId = Objects.requireNonNull(transactionId);
        this.operatorCode = Objects.requireNonNull(operatorCode);
        this.attemptNumber = attemptNumber;
        this.attemptedAt = Instant.now();
        this.successful = false;
    }

    // Reconstruction from persistence
    public TransactionAttempt(
            UUID id,
            UUID transactionId,
            OperatorCode operatorCode,
            int attemptNumber,
            Instant attemptedAt,
            String resultCode,
            String resultMessage,
            boolean successful) {
        this.id = id;
        this.transactionId = transactionId;
        this.operatorCode = operatorCode;
        this.attemptNumber = attemptNumber;
        this.attemptedAt = attemptedAt;
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
        this.successful = successful;
    }

    public void markSuccess(String resultCode, String resultMessage) {
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
        this.successful = true;
    }

    public void markFailure(String resultCode, String resultMessage) {
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
        this.successful = false;
    }

    public UUID getId() { return id; }
    public UUID getTransactionId() { return transactionId; }
    public OperatorCode getOperatorCode() { return operatorCode; }
    public int getAttemptNumber() { return attemptNumber; }
    public Instant getAttemptedAt() { return attemptedAt; }
    public String getResultCode() { return resultCode; }
    public String getResultMessage() { return resultMessage; }
    public boolean isSuccessful() { return successful; }
}
