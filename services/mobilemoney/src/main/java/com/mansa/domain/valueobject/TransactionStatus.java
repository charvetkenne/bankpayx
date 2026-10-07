package com.mansa.domain.valueobject;

import java.util.Set;

public enum TransactionStatus {

    INITIATED,
    PENDING,
    PROCESSING,
    SUCCEEDED,
    FAILED,
    CANCELLED,
    EXPIRED;

    private static final Set<TransactionStatus> TERMINAL_STATUSES =
            Set.of(SUCCEEDED, FAILED, CANCELLED, EXPIRED);

    private static final Set<TransactionStatus> RETRYABLE_STATUSES =
            Set.of(FAILED, EXPIRED);

    private static final Set<TransactionStatus> CANCELLABLE_STATUSES =
            Set.of(INITIATED, PENDING, FAILED);

    public boolean isTerminal() {
        return TERMINAL_STATUSES.contains(this);
    }

    public boolean isRetryable() {
        return RETRYABLE_STATUSES.contains(this);
    }

    public boolean isCancellable() {
        return CANCELLABLE_STATUSES.contains(this);
    }

    public boolean canTransitionTo(TransactionStatus next) {
        return switch (this) {
            case INITIATED -> next == PENDING || next == FAILED || next == CANCELLED;
            case PENDING -> next == PROCESSING || next == FAILED || next == CANCELLED || next == EXPIRED;
            case PROCESSING -> next == SUCCEEDED || next == FAILED;
            case FAILED -> next == INITIATED || next == CANCELLED;
            case EXPIRED -> next == INITIATED || next == CANCELLED;
            case SUCCEEDED, CANCELLED -> false;
        };
    }
}