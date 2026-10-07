package com.mansa.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Vue unifiée fusionnant les transactions Stripe et PayPal.
 * Utilisée par GET /api/v1/history pour afficher tout l'historique en une seule liste.
 */
@Getter
@Builder
public class UnifiedTransactionResponse {

    private String     transactionId;
    private String     paymentProvider;     // "STRIPE" ou "PAYPAL"
    private BigDecimal amount;
    private String     currency;
    private String     status;
    private String     merchantId;
    private String     description;
    private Instant    createdAt;
    private Instant    updatedAt;

    // ── Stripe uniquement ─────────────────────────────────────────────────────
    private String     maskedCardNumber;
    private String     cardType;
    private String     gatewayTransactionId;

    // ── PayPal uniquement ─────────────────────────────────────────────────────
    private String     paypalOrderId;
    private String     captureId;
    private String     approveUrl;
}
