package com.mansa.infrastructure.stripe.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StripePaymentResponse {
    private final String  paymentIntentId;
    private final String  status;
    private final boolean requiresAction;
    private final String  clientSecret;   // for 3DS redirection
    private final String  redirectUrl;    // pour les méthodes avec redirection externe

}
