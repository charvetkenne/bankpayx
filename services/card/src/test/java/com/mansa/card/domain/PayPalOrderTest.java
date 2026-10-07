package com.mansa.card.domain;

import com.mansa.domain.enums.Currency;
import com.mansa.domain.enums.PayPalOrderStatus;
import com.mansa.domain.event.PayPalOrderCapturedEvent;
import com.mansa.domain.event.PayPalOrderCreatedEvent;
import com.mansa.domain.model.Money;
import com.mansa.domain.model.PayPalOrder;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class PayPalOrderTest {

    private PayPalOrder buildOrder() {
        return PayPalOrder.initiate(
                Money.of(25.00, Currency.EUR),
                "merchant_1", "Commande #42",
                "http://localhost:3000/success",
                "http://localhost:3000/cancel",
                "corr-paypal-001"
        );
    }

    @Test
    void initiate_setsPendingApprovalStatus() {
        PayPalOrder order = buildOrder();
        assertThat(order.getStatus()).isEqualTo(PayPalOrderStatus.PENDING_APPROVAL);
        assertThat(order.getTransactionId()).isNotBlank();
    }

    @Test
    void markCreated_setsOrderIdAndApproveUrl() {
        PayPalOrder order = buildOrder();
        order.markCreated("ORDER-123", "https://sandbox.paypal.com/checkoutnow?token=ORDER-123");

        assertThat(order.getPaypalOrderId()).isEqualTo("ORDER-123");
        assertThat(order.getApproveUrl()).contains("ORDER-123");
        assertThat(order.pullDomainEvents()).hasSize(1)
                .first().isInstanceOf(PayPalOrderCreatedEvent.class);
    }

    @Test
    void markCaptured_setsStatusAndEmitsEvent() {
        PayPalOrder order = buildOrder();
        order.markCreated("ORDER-123", "https://sandbox.paypal.com/checkoutnow?token=ORDER-123");
        order.pullDomainEvents(); // vider

        order.markCaptured("CAPTURE-999");

        assertThat(order.getStatus()).isEqualTo(PayPalOrderStatus.CAPTURED);
        assertThat(order.getCaptureId()).isEqualTo("CAPTURE-999");
        assertThat(order.pullDomainEvents()).hasSize(1)
                .first().isInstanceOf(PayPalOrderCapturedEvent.class);
    }

    @Test
    void markFailed_setsFailureReason() {
        PayPalOrder order = buildOrder();
        order.markFailed("PayPal API error");
        assertThat(order.getStatus()).isEqualTo(PayPalOrderStatus.FAILED);
        assertThat(order.getFailureReason()).isEqualTo("PayPal API error");
    }
}
