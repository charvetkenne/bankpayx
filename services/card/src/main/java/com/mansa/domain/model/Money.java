package com.mansa.domain.model;

import com.mansa.domain.enums.Currency;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Money {

    private final BigDecimal amount;
    private final Currency   currency;

    private Money(BigDecimal amount, Currency currency) {
        this.amount   = amount.setScale(2, RoundingMode.HALF_UP);
        this.currency = currency;
    }

    public static Money of(BigDecimal amount, Currency currency) {
        Objects.requireNonNull(amount,   "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        if (amount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Amount must be non-negative");
        return new Money(amount, currency);
    }

    public static Money of(double amount, Currency currency) {
        return of(BigDecimal.valueOf(amount), currency);
    }

    public Money add(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    public boolean isGreaterThan(Money other) {
        assertSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    private void assertSameCurrency(Money other) {
        if (!this.currency.equals(other.currency))
            throw new IllegalArgumentException("Currency mismatch: " + this.currency + " vs " + other.currency);
    }

    public BigDecimal getAmount()   { return amount; }
    public Currency   getCurrency() { return currency; }

    /** Returns the amount in the smallest unit (cents) as long. */
    public long toSmallestUnit() {
        return amount.multiply(BigDecimal.valueOf(100)).longValueExact();
    }

    @Override public String toString() { return amount + " " + currency; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money m)) return false;
        return amount.equals(m.amount) && currency == m.currency;
    }

    @Override public int hashCode() { return Objects.hash(amount, currency); }
}
