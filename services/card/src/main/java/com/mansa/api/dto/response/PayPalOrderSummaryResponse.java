package com.mansa.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Vue compacte d'un order PayPal (pour les listes).
 */
@Getter
@Builder
public class PayPalOrderSummaryResponse {
    private String     transactionId;
    private String     paypalOrderId;
    private String     captureId;
    private BigDecimal amount;
    private String     currency;
    private String     status;
    private String     merchantId;
    private String     description;
    private Instant    createdAt;
    private Instant    updatedAt;
}
