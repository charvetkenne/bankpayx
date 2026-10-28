package com.mansa.domain.model;

import com.mansa.domain.enums.CardType;
import com.mansa.domain.exception.InvalidCardException;
import lombok.Builder;
import lombok.Getter;

import java.time.YearMonth;

@Getter
@Builder
public class Card {

    private final String    cardNumber;   // PAN (masked for storage)
    private final String    cardHolder;
    private final YearMonth expiryDate;
    private final String    cvv;          // never persisted
    private final CardType  cardType;

    public static Card create(String pan, String cardHolder, YearMonth expiryDate, String cvv) {
        validate(pan, cardHolder, expiryDate, cvv);
        return Card.builder()
                .cardNumber(pan)
                .cardHolder(cardHolder)
                .expiryDate(expiryDate)
                .cvv(cvv)
                .cardType(CardType.fromPan(pan))
                .build();
    }
 
    
    public String maskedNumber() {
        if (cardNumber == null || cardNumber.length() < 4) return "****";
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

    public boolean isExpired() {
        return expiryDate.isBefore(YearMonth.now());
    }

    private static void validate(String pan, String holder, YearMonth expiry, String cvv) {
        if (pan == null || pan.replaceAll("\\s", "").length() < 13)
            throw new InvalidCardException("Invalid card number");
        if (holder == null || holder.isBlank())
            throw new InvalidCardException("Card holder name is required");
        if (expiry == null || expiry.isBefore(YearMonth.now()))
            throw new InvalidCardException("Card is expired");
        if (cvv == null || !cvv.matches("\\d{3,4}"))
            throw new InvalidCardException("Invalid CVV");
    }
}
