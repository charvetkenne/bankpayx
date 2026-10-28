package com.mansa.domain.enums;

public enum CardType {
    VISA, MASTERCARD, AMEX, UNKNOWN;

    public static CardType fromPan(String pan) {
        if (pan == null || pan.isBlank()) return UNKNOWN;
        if (pan.startsWith("4"))         return VISA;
        if (pan.startsWith("5"))         return MASTERCARD;
        if (pan.startsWith("3"))         return AMEX;
        return UNKNOWN;
    }
}
