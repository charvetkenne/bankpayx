package com.mansa.infrastructure.persistence.entity;

import com.mansa.domain.enums.CardType;
import com.mansa.domain.enums.Currency;
import com.mansa.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_tx_gateway_id",     columnList = "gateway_transaction_id"),
    @Index(name = "idx_tx_gateway_pm_id",  columnList = "gateway_payment_method_id"),
    @Index(name = "idx_tx_merchant_id",    columnList = "merchant_id"),
    @Index(name = "idx_tx_status",         columnList = "status"),
    @Index(name = "idx_tx_correlation_id", columnList = "correlation_id")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TransactionEntity {

    @Id
    @Column(name = "transaction_id", nullable = false, updatable = false)
    private String transactionId;

    // ── Données carte (nullables — peuplées depuis la réponse Stripe) ─────────
    @Column(name = "masked_card_number", length = 25)
    private String maskedCardNumber;

    @Column(name = "card_holder", length = 100)
    private String cardHolder;

    @Column(name = "card_type", length = 15)
    @Enumerated(EnumType.STRING)
    private CardType cardType;

    // ── Montant ───────────────────────────────────────────────────────────────
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 5)
    @Enumerated(EnumType.STRING)
    private Currency currency;

    // ── Merchant / contexte ───────────────────────────────────────────────────
    @Column(name = "merchant_id", nullable = false, length = 100)
    private String merchantId;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    // ── Gateway ───────────────────────────────────────────────────────────────
    @Column(name = "gateway_payment_method_id", length = 200)
    private String gatewayPaymentMethodId;   // pm_xxx — token Stripe.js

    @Column(name = "gateway_transaction_id", length = 200)
    private String gatewayTransactionId;     // pi_xxx — PaymentIntent Stripe

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    // ── Statut / timestamps ───────────────────────────────────────────────────
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
