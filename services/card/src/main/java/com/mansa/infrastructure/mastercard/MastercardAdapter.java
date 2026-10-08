package com.mansa.infrastructure.mastercard;

import com.mansa.application.port.out.CardValidationPort;
import com.mansa.domain.enums.CardType;
import com.mansa.domain.model.Card;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Implements Mastercard-specific card-network validation rules.
 * Mastercard PANs start with 51–55 or 2221–2720.
 */
@Slf4j
@Component
public class MastercardAdapter implements CardValidationPort {

    @Override
    public boolean supports(Card card) {
        return card.getCardType() == CardType.MASTERCARD;
    }

    @Override
    public boolean validate(Card card) {
        String pan = card.getCardNumber().replaceAll("\\s|-", "");

        if (!isMastercardBin(pan)) {
            log.warn("Mastercard validation: BIN check failed for PAN starting {}", pan.substring(0, 4));
            return false;
        }
        if (pan.length() != 16) {
            log.warn("Mastercard validation: invalid length {}", pan.length());
            return false;
        }
        if (card.isExpired()) {
            log.warn("Mastercard validation: card is expired");
            return false;
        }
        return true;
    }

    private boolean isMastercardBin(String pan) {
        int prefix2 = Integer.parseInt(pan.substring(0, 2));
        int prefix4 = Integer.parseInt(pan.substring(0, 4));
        return (prefix2 >= 51 && prefix2 <= 55) || (prefix4 >= 2221 && prefix4 <= 2720);
    }
}
