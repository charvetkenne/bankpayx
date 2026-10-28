package com.mansa.card.infrastructure;

import com.mansa.domain.enums.CardType;
import com.mansa.domain.model.Card;
import com.mansa.infrastructure.visa.VisaAdapter;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

class VisaAdapterTest {

    VisaAdapter adapter = new VisaAdapter();

    @Test
    void supports_onlyVisaCards() {
        Card visa = Card.builder().cardType(CardType.VISA).cardNumber("4111111111111111")
                .cardHolder("Test").expiryDate(YearMonth.now().plusYears(1)).build();
        Card mc   = Card.builder().cardType(CardType.MASTERCARD).cardNumber("5111111111111118")
                .cardHolder("Test").expiryDate(YearMonth.now().plusYears(1)).build();
        assertThat(adapter.supports(visa)).isTrue();
        assertThat(adapter.supports(mc)).isFalse();
    }

    @Test
    void validate_validVisaCard_returnsTrue() {
        Card card = Card.builder()
                .cardType(CardType.VISA)
                .cardNumber("4111111111111111")
                .cardHolder("John Doe")
                .expiryDate(YearMonth.now().plusYears(2))
                .build();
        assertThat(adapter.validate(card)).isTrue();
    }

    @Test
    void validate_expiredCard_returnsFalse() {
        Card card = Card.builder()
                .cardType(CardType.VISA)
                .cardNumber("4111111111111111")
                .cardHolder("John Doe")
                .expiryDate(YearMonth.of(2020, 1))
                .build();
        assertThat(adapter.validate(card)).isFalse();
    }
}
