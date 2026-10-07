package com.mansa.application.port.out;

import com.mansa.domain.model.Card;

public interface CardValidationPort {

    /**
     * Validate card-network specific rules (BIN check, card type, etc.)
     */
    boolean validate(Card card);

    /**
     * Returns true if this validator supports the given card (e.g. VISA-only).
     */
    boolean supports(Card card);
}
