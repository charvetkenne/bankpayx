package com.mansa.card.domain;

import com.mansa.domain.enums.Currency;
import com.mansa.domain.model.Money;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class MoneyTest {

    @Test
    void of_validAmount_createsMoney() {
        Money money = Money.of(25.00, Currency.EUR);
        assertThat(money.getAmount().doubleValue()).isEqualTo(25.00);
        assertThat(money.getCurrency()).isEqualTo(Currency.EUR);
    }

    @Test
    void of_negativeAmount_throwsException() {
        assertThatThrownBy(() -> Money.of(-1.0, Currency.EUR))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non-negative");
    }

    @Test
    void toSmallestUnit_convertsCorrectly() {
        Money money = Money.of(25.50, Currency.EUR);
        assertThat(money.toSmallestUnit()).isEqualTo(2550L);
    }

    @Test
    void add_sameCurrency_addsAmounts() {
        Money a = Money.of(10.00, Currency.EUR);
        Money b = Money.of(15.00, Currency.EUR);
        assertThat(a.add(b).getAmount().doubleValue()).isEqualTo(25.00);
    }

    @Test
    void add_differentCurrency_throwsException() {
        Money eur = Money.of(10.00, Currency.EUR);
        Money usd = Money.of(10.00, Currency.USD);
        assertThatThrownBy(() -> eur.add(usd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Currency mismatch");
    }
}
