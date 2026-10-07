package com.mansa.application.port.in;

import com.mansa.domain.model.CardPayment;

public interface ProcessCardPaymentUseCase {
 
    /**
     * Le Command ne contient plus aucune donnée brute de carte.
     * Le paymentMethodId (pm_xxx) est le seul identifiant de la carte —
     * généré par Stripe.js côté frontend, jamais par le backend.
     */
    record Command(
            String paymentMethodId,   // pm_xxx — token Stripe.js
            Double amount,
            String currency,
            String merchantId,
            String description,
            String correlationId
    ) {}
 
    CardPayment process(Command command);
}
/*
 record Command(
            String  pan, <--supprimer
            String  cardHolder,
            Integer expiryMonth, <-- supprimer
            Integer expiryYear,<-- supprime
            String  cvv,
            Double  amount,
            String  currency,
            String  merchantId,
            String  description,
            String  correlationId,
            String  paymentMethodId
    ) {}
*/