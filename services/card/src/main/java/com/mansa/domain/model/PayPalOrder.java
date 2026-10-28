package com.mansa.domain.model;

import com.mansa.domain.enums.Currency;
import com.mansa.domain.enums.PayPalOrderStatus;
import com.mansa.domain.event.DomainEvent;
import com.mansa.domain.event.PayPalOrderCapturedEvent;
import com.mansa.domain.event.PayPalOrderCreatedEvent;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class PayPalOrder {

    private final String             transactionId;  // ID interne
    private       String             paypalOrderId;  // ID PayPal (ORDER-xxx)
    private       String             captureId;      // ID de capture PayPal
    private final Money              amount;
    private final String             merchantId;
    private final String             description;
    private final String             returnUrl;
    private final String             cancelUrl;
    private final String             correlationId;
    private       String             approveUrl;
    private       PayPalOrderStatus  status;
    private       String             failureReason;
    private final Instant            createdAt;
    private       Instant            updatedAt;

    @Builder.Default
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // ── Factory ───────────────────────────────────────────────────────────────
    public static PayPalOrder initiate(Money amount, String merchantId, String description,
                                       String returnUrl, String cancelUrl, String correlationId) {
        return PayPalOrder.builder()
                .transactionId(UUID.randomUUID().toString())
                .amount(amount)
                .merchantId(merchantId)
                .description(description)
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .correlationId(correlationId)
                .status(PayPalOrderStatus.PENDING_APPROVAL)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    // ── Transitions ───────────────────────────────────────────────────────────
    public void markCreated(String paypalOrderId, String approveUrl) {
        this.paypalOrderId = paypalOrderId;
        this.approveUrl    = approveUrl;
        this.status        = PayPalOrderStatus.PENDING_APPROVAL;
        this.updatedAt     = Instant.now();
        domainEvents.add(new PayPalOrderCreatedEvent(transactionId, paypalOrderId, correlationId));
    }

    public void markCaptured(String captureId) {
        this.captureId = captureId;
        this.status    = PayPalOrderStatus.CAPTURED;
        this.updatedAt = Instant.now();
        domainEvents.add(new PayPalOrderCapturedEvent(transactionId, paypalOrderId, captureId, amount, correlationId));
    }

    public void markFailed(String reason) {
        this.failureReason = reason;
        this.status        = PayPalOrderStatus.FAILED;
        this.updatedAt     = Instant.now();
    }

    public void markCancelled() {
        this.status    = PayPalOrderStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return Collections.unmodifiableList(events);
    }
}
