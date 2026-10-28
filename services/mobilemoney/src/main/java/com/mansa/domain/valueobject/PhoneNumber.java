package com.mansa.domain.valueobject;


import java.util.Objects;
import java.util.regex.Pattern;

public record PhoneNumber(String value) {

    private static final Pattern INTERNATIONAL_PHONE_PATTERN =
            Pattern.compile("^\\+?[1-9]\\d{6,14}$");

    public PhoneNumber {
        Objects.requireNonNull(value, "PhoneNumber must not be null");
        String normalized = value.trim().replaceAll("\\s+", "");
        if (!INTERNATIONAL_PHONE_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                    "Invalid phone number format: " + value +
                    ". Expected international format, e.g. +22507XXXXXXXX"
            );
        }
        value = normalized;
    }

    public static PhoneNumber of(String value) {
        return new PhoneNumber(value);
    }

    public String countryCode() {
        if (value.startsWith("+")) {
            // Simplified: returns first 1-3 digits after +
            return value.substring(1, Math.min(4, value.length()));
        }
        return "";
    }

    public String localNumber() {
        if (value.startsWith("+")) {
            return value.substring(1);
        }
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}