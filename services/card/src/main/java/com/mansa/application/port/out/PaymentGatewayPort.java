package com.mansa.application.port.out;

import com.mansa.domain.model.CardPayment;

public interface PaymentGatewayPort {

    /**
     * Authorize a payment via the payment gateway (Stripe, etc.).
     * Returns the gateway transaction ID on success.
     */
    String authorize(CardPayment payment);

    /**
     * Capture a previously authorized payment.
     */
    void capture(String gatewayTransactionId);

    /**
     * Refund a captured payment (full or partial).
     */
    void refund(String gatewayTransactionId);
}
