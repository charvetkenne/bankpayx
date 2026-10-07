package com.mansa.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Vue compacte d'une transaction Stripe (pour les listes).
 */
@Getter
@Builder
public class TransactionSummaryResponse {
    private String     transactionId;
    private String     maskedCardNumber;
    private String     cardType;
    private BigDecimal amount;
    private String     currency;
    private String     status;
    private String     merchantId;
    private String     description;
    private String     gatewayTransactionId;
    private Instant    createdAt;
    private Instant    updatedAt;
}
