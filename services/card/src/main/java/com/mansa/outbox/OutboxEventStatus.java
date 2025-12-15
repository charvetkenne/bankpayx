package com.mansa.outbox;

public enum OutboxEventStatus {
    PENDING,
    SENDING,
    SENT,
    FAILED
}

