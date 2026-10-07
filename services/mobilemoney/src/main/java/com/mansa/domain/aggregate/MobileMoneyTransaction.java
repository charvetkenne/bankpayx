package com.mansa.domain.aggregate;


import com.mansa.domain.entity.TransactionAttempt;
import com.mansa.domain.event.*;
import com.mansa.domain.execption.InvalidTransactionStateException;
import com.mansa.domain.valueobject.IdempotencyKey;
import com.mansa.domain.valueobject.Money;
import com.mansa.domain.valueobject.OperatorCode;
import com.mansa.domain.valueobject.OperatorReference;
import com.mansa.domain.valueobject.PhoneNumber;
import com.mansa.domain.valueobject.TransactionId;
import com.mansa.domain.valueobject.TransactionStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MobileMoneyTransaction {

    private static final int MAX_RETRY_ATTEMPTS = 3;

    private final TransactionId id;
    private final IdempotencyKey idempotencyKey;
    private final PhoneNumber phoneNumber;
    private final Money amount;
    private OperatorCode operatorCode;
    private TransactionStatus status;
    private OperatorReference operatorReference;
    private final String customerId;
    private final String correlationId;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;
    private String failureReason;
    private String failureCode;
    private String cancellationReason;
    private int retryCount;
    private final List<TransactionAttempt> attempts;
    private final List<DomainEvent> domainEvents;

    // ---- Factory method (new transaction) ----
    public static MobileMoneyTransaction initiate(
            PhoneNumber phoneNumber,
            Money amount,
            OperatorCode operatorCode,
            String customerId,
            IdempotencyKey idempotencyKey,
            String correlationId) {

        Objects.requireNonNull(phoneNumber, "PhoneNumber required");
        Objects.requireNonNull(amount, "Amount required");
        Objects.requireNonNull(operatorCode, "OperatorCode required");
        Objects.requireNonNull(customerId, "CustomerId required");
        Objects.requireNonNull(idempotencyKey, "IdempotencyKey required");
        Objects.requireNonNull(correlationId, "CorrelationId required");

        if (amount.isZero()) {
            throw new IllegalArgumentException("Transaction amount must be greater than zero");
        }

        MobileMoneyTransaction tx = new MobileMoneyTransaction(
                TransactionId.generate(),
                phoneNumber,
                amount,
                operatorCode,
                customerId,
                idempotencyKey,
                correlationId
        );

        tx.domainEvents.add(new PaymentInitiatedEvent(
                correlationId, tx.id, phoneNumber, amount, operatorCode, idempotencyKey
        ));

        return tx;
    }

    // ---- Private constructor for new transaction ----
    private MobileMoneyTransaction(
            TransactionId id,
            PhoneNumber phoneNumber,
            Money amount,
            OperatorCode operatorCode,
            String customerId,
            IdempotencyKey idempotencyKey,
            String correlationId) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.amount = amount;
        this.operatorCode = operatorCode;
        this.customerId = customerId;
        this.idempotencyKey = idempotencyKey;
        this.correlationId = correlationId;
        this.status = TransactionStatus.INITIATED;
        this.retryCount = 0;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.attempts = new ArrayList<>();
        this.domainEvents = new ArrayList<>();
    }

    // ---- Reconstruction constructor (from persistence) ----
    public MobileMoneyTransaction(
            TransactionId id,
            IdempotencyKey idempotencyKey,
            PhoneNumber phoneNumber,
            Money amount,
            OperatorCode operatorCode,
            TransactionStatus status,
            OperatorReference operatorReference,
            String customerId,
            String correlationId,
            Instant createdAt,
            Instant updatedAt,
            Instant completedAt,
            String failureReason,
            String failureCode,
            String cancellationReason,
            int retryCount,
            List<TransactionAttempt> attempts) {
        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.phoneNumber = phoneNumber;
        this.amount = amount;
        this.operatorCode = operatorCode;
        this.status = status;
        this.operatorReference = operatorReference;
        this.customerId = customerId;
        this.correlationId = correlationId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.completedAt = completedAt;
        this.failureReason = failureReason;
        this.failureCode = failureCode;
        this.cancellationReason = cancellationReason;
        this.retryCount = retryCount;
        this.attempts = new ArrayList<>(attempts);
        this.domainEvents = new ArrayList<>();
    }

    // ---- Business methods ----

    public void markAsPending() {
        requireTransition(TransactionStatus.PENDING);
        this.status = TransactionStatus.PENDING;
        this.updatedAt = Instant.now();
    }

    public void markAsProcessing() {
        requireTransition(TransactionStatus.PROCESSING);
        this.status = TransactionStatus.PROCESSING;
        this.updatedAt = Instant.now();
    }

    public void markAsSucceeded(OperatorReference operatorReference) {
        requireTransition(TransactionStatus.SUCCEEDED);
        Objects.requireNonNull(operatorReference, "OperatorReference required on success");
        this.status = TransactionStatus.SUCCEEDED;
        this.operatorReference = operatorReference;
        this.completedAt = Instant.now();
        this.updatedAt = this.completedAt;

        this.domainEvents.add(new PaymentSucceededEvent(
                correlationId, id, operatorReference, amount, operatorCode, this.completedAt
        ));
    }

    public void markAsFailed(String failureCode, String failureReason) {
        requireTransition(TransactionStatus.FAILED);
        this.status = TransactionStatus.FAILED;
        this.failureCode = failureCode;
        this.failureReason = failureReason;
        this.updatedAt = Instant.now();

        this.domainEvents.add(new PaymentFailedEvent(
                correlationId, id, operatorCode, failureReason, failureCode
        ));
    }

    public void cancel(String cancellationReason) {
        if (!this.status.isCancellable()) {
            throw new InvalidTransactionStateException(this.status, TransactionStatus.CANCELLED);
        }
        this.status = TransactionStatus.CANCELLED;
        this.cancellationReason = cancellationReason;
        this.completedAt = Instant.now();
        this.updatedAt = this.completedAt;

        this.domainEvents.add(new PaymentCancelledEvent(
                correlationId, id, operatorCode, cancellationReason
        ));
    }

    public void retryWith(OperatorCode retryOperatorCode) {
        if (!this.status.isRetryable()) {
            throw new InvalidTransactionStateException(this.status, TransactionStatus.INITIATED);
        }
        if (this.retryCount >= MAX_RETRY_ATTEMPTS) {
            throw new IllegalStateException(
                    "Maximum retry attempts (" + MAX_RETRY_ATTEMPTS + ") reached for transaction " + id
            );
        }
        this.operatorCode = retryOperatorCode;
        this.status = TransactionStatus.INITIATED;
        this.failureReason = null;
        this.failureCode = null;
        this.retryCount++;
        this.updatedAt = Instant.now();
    }

    public void switchToFallbackOperator(OperatorCode fallbackOperator) {
        Objects.requireNonNull(fallbackOperator, "Fallback operator required");
        this.operatorCode = fallbackOperator;
        this.updatedAt = Instant.now();
    }

    public TransactionAttempt recordAttempt() {
        TransactionAttempt attempt = new TransactionAttempt(
                id.value(), operatorCode, attempts.size() + 1
        );
        attempts.add(attempt);
        return attempt;
    }

    public boolean canRetry() {
        return this.status.isRetryable() && this.retryCount < MAX_RETRY_ATTEMPTS;
    }

    public boolean isTerminal() {
        return this.status.isTerminal();
    }

    private void requireTransition(TransactionStatus target) {
        if (!this.status.canTransitionTo(target)) {
            throw new InvalidTransactionStateException(this.status, target);
        }
    }

    // ---- Getters ----

    public TransactionId getId() { return id; }
    public IdempotencyKey getIdempotencyKey() { return idempotencyKey; }
    public PhoneNumber getPhoneNumber() { return phoneNumber; }
    public Money getAmount() { return amount; }
    public OperatorCode getOperatorCode() { return operatorCode; }
    public TransactionStatus getStatus() { return status; }
    public OperatorReference getOperatorReference() { return operatorReference; }
    public String getCustomerId() { return customerId; }
    public String getCorrelationId() { return correlationId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public String getFailureReason() { return failureReason; }
    public String getFailureCode() { return failureCode; }
    public String getCancellationReason() { return cancellationReason; }
    public int getRetryCount() { return retryCount; }
    public List<TransactionAttempt> getAttempts() { return Collections.unmodifiableList(attempts); }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }

    public static int getMaxRetryAttempts() {
        return MAX_RETRY_ATTEMPTS;
    }
}
