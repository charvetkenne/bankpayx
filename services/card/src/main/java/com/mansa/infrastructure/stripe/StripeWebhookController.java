package com.mansa.infrastructure.stripe;

// import com.mansa.application.port.in.CapturePaymentUseCase;
// import com.mansa.application.port.out.TransactionPersistencePort;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

/**
 * Receives Stripe webhook events (payment_intent.succeeded, payment_intent.payment_failed, …).
 *
 * Signature verification should be added via StripeConfig.webhookSecret.
 * See: https://stripe.com/docs/webhooks/signatures
 */

import com.mansa.application.port.in.CapturePaymentUseCase;
import com.mansa.application.port.out.TransactionPersistencePort;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Reçoit les webhooks Stripe via POST /webhooks/stripe.
 *
 * Événements traités :
 *   payment_intent.requires_capture  → capture automatique (après 3DS)
 *   payment_intent.payment_failed    → mise à jour du statut en FAILED
 *   charge.refund.updated            → suivi du remboursement
 *
 * La vérification de la signature Stripe est ACTIVÉE :
 * chaque requête est validée avec le webhook-secret de StripeConfig.
 */
@Slf4j
@RestController
@RequestMapping("/webhooks/stripe")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final StripeConfig               stripeConfig;
    private final CapturePaymentUseCase      captureUseCase;
    private final TransactionPersistencePort persistencePort;
    //private final StripePaymentProcessor     paymentProcessor;

    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {

        if (sigHeader == null || sigHeader.isBlank()) {
            log.warn("Stripe webhook received without Stripe-Signature header – rejected");
            return ResponseEntity.badRequest().body("Missing signature");
        }

        // ── 1. Vérification de la signature ───────────────────────────────────
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeConfig.getWebhookSecret());
        } catch (SignatureVerificationException ex) {
            log.warn("Stripe webhook signature verification failed: {}", ex.getMessage());
            return ResponseEntity.status(400).body("Invalid signature");
        }

        log.info("Stripe webhook received: type={} id={}", event.getType(), event.getId());

        // ── 2. Dispatch selon le type d'événement ─────────────────────────────
        switch (event.getType()) {

            case "payment_intent.requires_capture" -> {
                extractPaymentIntent(event).ifPresent(this::handleRequiresCapture);
            }

            case "payment_intent.succeeded" -> {
                extractPaymentIntent(event).ifPresent(intent ->
                    log.info("PaymentIntent succeeded: id={}", intent.getId()));
            }

            case "payment_intent.payment_failed" -> {
                extractPaymentIntent(event).ifPresent(this::handlePaymentFailed);
            }

            case "charge.refund.updated" ->
                log.info("Refund updated for event id={}", event.getId());

            default ->
                log.debug("Stripe webhook event ignored: type={}", event.getType());
        }

        return ResponseEntity.ok("OK");
    }

    // ── Handlers ──────────────────────────────────────────────────────────────

    private void handleRequiresCapture(PaymentIntent intent) {
        log.info("PaymentIntent requires capture: id={}", intent.getId());
        persistencePort.findByGatewayTransactionId(intent.getId()).ifPresentOrElse(
                payment -> {
                    captureUseCase.capture(payment.getTransactionId());
                    log.info("Auto-capture triggered for transactionId={}", payment.getTransactionId());
                },
                () -> log.warn("No transaction found for paymentIntentId={}", intent.getId())
        );
    }

    private void handlePaymentFailed(PaymentIntent intent) {
        String reason = intent.getLastPaymentError() != null
                ? intent.getLastPaymentError().getMessage()
                : "Unknown failure";
        log.warn("PaymentIntent failed: id={} reason={}", intent.getId(), reason);
        // Le statut FAILED est normalement déjà positionné par CardPaymentService.
        // Ici on peut déclencher une notification / alerting supplémentaire.
    }

    // ── Helper de désérialisation ─────────────────────────────────────────────

    private Optional<PaymentIntent> extractPaymentIntent(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        if (!deserializer.getObject().isPresent()) {
            log.warn("Could not deserialize PaymentIntent from event id={}", event.getId());
            return Optional.empty();
        }
        StripeObject obj = deserializer.getObject().get();
        if (obj instanceof PaymentIntent pi) {
            return Optional.of(pi);
        }
        log.warn("Expected PaymentIntent but got {} for event id={}",
                obj.getClass().getSimpleName(), event.getId());
        return Optional.empty();
    }
}
