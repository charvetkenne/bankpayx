package com.mansa.card.domain;

import com.mansa.domain.enums.Currency;
import com.mansa.domain.enums.PaymentStatus;
import com.mansa.domain.event.PaymentAuthorizedEvent;
import com.mansa.domain.event.PaymentFailedEvent;
import com.mansa.domain.model.CardPayment;
import com.mansa.domain.model.Money;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class CardPaymentTest {

    private CardPayment buildPayment() {
        return CardPayment.initiate(
                Money.of(50.00, Currency.EUR),
                "merchant_1", "Test payment", "corr-001", "pm_test_123"
        );
    }

    @Test
    void initiate_setsStatusPending() {
        CardPayment payment = buildPayment();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.getTransactionId()).isNotBlank();
    }

    @Test
    void markAuthorized_changesStatusAndEmitsEvent() {
        CardPayment payment = buildPayment();
        payment.markAuthorized("pi_test_001");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
        assertThat(payment.getGatewayTransactionId()).isEqualTo("pi_test_001");

        var events = payment.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(PaymentAuthorizedEvent.class);
    }

    @Test
    void markFailed_changesStatusAndEmitsEvent() {
        CardPayment payment = buildPayment();
        payment.markFailed("Insufficient funds");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(payment.getFailureReason()).isEqualTo("Insufficient funds");

        var events = payment.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(PaymentFailedEvent.class);
    }

    @Test
    void pullDomainEvents_clearsEventList() {
        CardPayment payment = buildPayment();
        payment.markAuthorized("pi_001");

        payment.pullDomainEvents(); // première lecture
        assertThat(payment.pullDomainEvents()).isEmpty(); // deuxième lecture
    }
}
