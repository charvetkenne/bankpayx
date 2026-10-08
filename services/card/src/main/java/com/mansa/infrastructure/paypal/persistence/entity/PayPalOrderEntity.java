package com.mansa.infrastructure.paypal.persistence.entity;

import com.mansa.domain.enums.Currency;
import com.mansa.domain.enums.PayPalOrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "paypal_orders", indexes = {
    @Index(name = "idx_pp_order_id",     columnList = "paypal_order_id"),
    @Index(name = "idx_pp_capture_id",   columnList = "paypal_capture_id"),
    @Index(name = "idx_pp_merchant_id",  columnList = "merchant_id"),
    @Index(name = "idx_pp_status",       columnList = "status"),
    @Index(name = "idx_pp_correlation",  columnList = "correlation_id")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PayPalOrderEntity {

    @Id
    @Column(name = "transaction_id", nullable = false, updatable = false)
    private String transactionId;

    @Column(name = "paypal_order_id", length = 200)
    private String paypalOrderId;

    @Column(name = "paypal_capture_id", length = 200)
    private String captureId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 5)
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(name = "merchant_id", nullable = false, length = 100)
    private String merchantId;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "return_url", length = 500)
    private String returnUrl;

    @Column(name = "cancel_url", length = 500)
    private String cancelUrl;

    @Column(name = "approve_url", length = 1000)
    private String approveUrl;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "status", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private PayPalOrderStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
