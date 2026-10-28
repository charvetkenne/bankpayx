package com.mansa.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record TransactionId(UUID value) {

    public TransactionId {
        Objects.requireNonNull(value, "TransactionId value must not be null");
    }

    public static TransactionId generate() {
        return new TransactionId(UUID.randomUUID());
    }

    public static TransactionId of(String value) {
        Objects.requireNonNull(value, "TransactionId string value must not be null");
        return new TransactionId(UUID.fromString(value));
    }

    public static TransactionId of(UUID value) {
        return new TransactionId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}