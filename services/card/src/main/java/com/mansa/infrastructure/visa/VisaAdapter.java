package com.mansa.infrastructure.visa;

import com.mansa.application.port.out.CardValidationPort;
import com.mansa.domain.enums.CardType;
import com.mansa.domain.model.Card;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Implements VISA-specific card-network validation rules.
 * VISA != Stripe. VISA is the card network, Stripe is the payment gateway.
 */
@Slf4j
@Component
public class VisaAdapter implements CardValidationPort {

    private static final int MIN_PAN_LENGTH = 13;
    private static final int MAX_PAN_LENGTH = 19;

    @Override
    public boolean supports(Card card) {
        return card.getCardType() == CardType.VISA;
    }

    @Override
    public boolean validate(Card card) {
        String pan = card.getCardNumber().replaceAll("\\s|-", "");

        if (!pan.startsWith("4")) {
            log.warn("VISA validation: PAN does not start with 4");
            return false;
        }
        if (pan.length() < MIN_PAN_LENGTH || pan.length() > MAX_PAN_LENGTH) {
            log.warn("VISA validation: invalid PAN length {}", pan.length());
            return false;
        }
        if (!luhnCheck(pan)) {
            log.warn("VISA validation: Luhn check failed");
            return false;
        }
        if (card.isExpired()) {
            log.warn("VISA validation: card is expired");
            return false;
        }
        return true;
    }

    /** Standard Luhn algorithm */
    private boolean luhnCheck(String pan) {
        int sum = 0;
        boolean alternate = false;
        for (int i = pan.length() - 1; i >= 0; i--) {
            int n = Character.getNumericValue(pan.charAt(i));
            if (alternate) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            sum += n;
            alternate = !alternate;
        }
        return sum % 10 == 0;
    }
}
