package com.mansa.domain.valueobject;

import java.util.Objects;

public record IdempotencyKey(String value) {

    public IdempotencyKey {
        Objects.requireNonNull(value, "IdempotencyKey must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("IdempotencyKey must not be blank");
        }
        if (value.length() > 128) {
            throw new IllegalArgumentException(
                    "IdempotencyKey must not exceed 128 characters, got: " + value.length()
            );
        }
    }

    public static IdempotencyKey of(String value) {
        return new IdempotencyKey(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
