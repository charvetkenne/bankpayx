package com.mansa.domain.model;

import com.mansa.domain.enums.CardType;
import com.mansa.domain.enums.PaymentStatus;
import com.mansa.domain.event.DomainEvent;
import com.mansa.domain.event.PaymentAuthorizedEvent;
import com.mansa.domain.event.PaymentCapturedEvent;
import com.mansa.domain.event.PaymentFailedEvent;
import com.mansa.domain.event.RefundCreatedEvent;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


@Getter
@Builder
public class CardPayment {

    private final String        transactionId;
    private final Money         amount;
    private final String        merchantId;
    private final String        description;
    private final String        correlationId;
    private final String        gatewayPaymentMethodId;
    private       String        maskedCardNumber;
    private       CardType      cardType;
    private       String        cardHolder;
    private       String        gatewayTransactionId;
    private       PaymentStatus status;
    private       String        failureReason;
    private       boolean       requiresAction;
    private       String        clientSecret;
    private       String        redirectUrl;
    private final Instant       createdAt;
    private       Instant       updatedAt;

    @Builder.Default
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public static CardPayment initiate(Money amount, String merchantId, String description,
                                       String correlationId, String gatewayPaymentMethodId) {
        return CardPayment.builder()
                .transactionId(UUID.randomUUID().toString())
                .amount(amount).merchantId(merchantId).description(description)
                .correlationId(correlationId).gatewayPaymentMethodId(gatewayPaymentMethodId)
                .status(PaymentStatus.PENDING)
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .build();
    }

    public void markAuthorized(String gatewayTransactionId) {
        this.status = PaymentStatus.AUTHORIZED;
        this.gatewayTransactionId = gatewayTransactionId;
        this.requiresAction = false;
        this.updatedAt = Instant.now();
        domainEvents.add(new PaymentAuthorizedEvent(transactionId, amount, correlationId));
    }

    /** Paiement en attente d'une action frontend (3DS iframe ou redirection externe). */
    public void markRequiresAction(String gatewayTransactionId, String clientSecret, String redirectUrl) {
        this.status = PaymentStatus.PENDING;
        this.gatewayTransactionId = gatewayTransactionId;
        this.requiresAction = true;
        this.clientSecret   = clientSecret;
        this.redirectUrl    = redirectUrl;
        this.updatedAt      = Instant.now();
    }

    public void markCaptured() {
        this.status = PaymentStatus.CAPTURED;
        this.updatedAt = Instant.now();
        domainEvents.add(new PaymentCapturedEvent(transactionId, amount, correlationId));
    }

    public void markFailed(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.updatedAt = Instant.now();
        domainEvents.add(new PaymentFailedEvent(transactionId, reason, correlationId));
    }

    public void markRefunded() {
        this.status = PaymentStatus.REFUNDED;
        this.updatedAt = Instant.now();
        domainEvents.add(new RefundCreatedEvent(transactionId, amount, correlationId));
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return Collections.unmodifiableList(events);
    }
}









/**
 * Agrégat principal du domaine.
 *
 * Les données brutes de carte (PAN, CVV, expiry) ne transitent PLUS par le backend.
 * Stripe.js les tokenise côté frontend → on reçoit uniquement le paymentMethodId (pm_xxx).
 * Les informations affichables (last4, brand) sont récupérées depuis la réponse Stripe
 * et stockées ici après autorisation.
 */
// @Getter
// @Builder
// public class CardPayment {
 
//     private final String        transactionId;
//     private final Money         amount;
//     private final String        merchantId;
//     private final String        description;
//     private final String        correlationId;
 
//     // Identifiant Stripe du moyen de paiement (pm_xxx)
//     private final String        gatewayPaymentMethodId;
 
//     // Données affichables récupérées depuis Stripe après autorisation
//     // (nullables — non disponibles avant le premier appel Stripe)
//     private       String        maskedCardNumber;      // ex: **** **** **** 4242
//     private       CardType      cardType;              // VISA, MASTERCARD…
//     private       String        cardHolder;            // nom du porteur si disponible
 
//     // Données gateway
//     private       String        gatewayTransactionId;
//     private       PaymentStatus status;
//     private       String        failureReason;
 
//     private final Instant       createdAt;
//     private       Instant       updatedAt;
 
//     @Builder.Default
//     private final List<DomainEvent> domainEvents = new ArrayList<>();
 
//     // ── Factory ───────────────────────────────────────────────────────────────
 
//     public static CardPayment initiate(Money amount, String merchantId, String description,
//                                        String correlationId, String gatewayPaymentMethodId) {
//         return CardPayment.builder()
//                 .transactionId(UUID.randomUUID().toString())
//                 .amount(amount)
//                 .merchantId(merchantId)
//                 .description(description)
//                 .correlationId(correlationId)
//                 .gatewayPaymentMethodId(gatewayPaymentMethodId)
//                 .status(PaymentStatus.PENDING)
//                 .createdAt(Instant.now())
//                 .updatedAt(Instant.now())
//                 .build();
//     }
 
//     // ── Transitions de statut ─────────────────────────────────────────────────
 
//     public void markAuthorized(String gatewayTransactionId) {
//         this.status               = PaymentStatus.AUTHORIZED;
//         this.gatewayTransactionId = gatewayTransactionId;
//         this.updatedAt            = Instant.now();
//         domainEvents.add(new PaymentAuthorizedEvent(this.transactionId, this.amount, this.correlationId));
//     }
 
//     public void markAuthorized(String gatewayTransactionId,
//                                 String maskedCardNumber, CardType cardType, String cardHolder) {
//         markAuthorized(gatewayTransactionId);
//         this.maskedCardNumber = maskedCardNumber;
//         this.cardType         = cardType;
//         this.cardHolder       = cardHolder;
//     }
 
//     public void markCaptured() {
//         this.status    = PaymentStatus.CAPTURED;
//         this.updatedAt = Instant.now();
//         domainEvents.add(new PaymentCapturedEvent(this.transactionId, this.amount, this.correlationId));
//     }
 
//     public void markFailed(String reason) {
//         this.status        = PaymentStatus.FAILED;
//         this.failureReason = reason;
//         this.updatedAt     = Instant.now();
//         domainEvents.add(new PaymentFailedEvent(this.transactionId, reason, this.correlationId));
//     }
 
//     public void markRefunded() {
//         this.status    = PaymentStatus.REFUNDED;
//         this.updatedAt = Instant.now();
//         domainEvents.add(new RefundCreatedEvent(this.transactionId, this.amount, this.correlationId));
//     }
 
//     public List<DomainEvent> pullDomainEvents() {
//         List<DomainEvent> events = new ArrayList<>(domainEvents);
//         domainEvents.clear();
//         return Collections.unmodifiableList(events);
//     }
// }