package com.mansa.infrastructure.stripe.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StripePaymentRequest {
    private final long   amountInCents;
    private final String currency;
    private final String description;
    private final String merchantId;
    private final String correlationId;
    // In production, use a Stripe PaymentMethod ID (tokenised by Stripe.js)
    private final String paymentMethodId;
}
