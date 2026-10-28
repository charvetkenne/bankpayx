package com.mansa.domain.valueobject;


import java.util.Objects;

public record OperatorReference(String value) {

    public OperatorReference {
        Objects.requireNonNull(value, "OperatorReference must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("OperatorReference must not be blank");
        }
    }

    public static OperatorReference of(String value) {
        return new OperatorReference(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
