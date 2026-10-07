package com.mansa.infrastructure.stripe;


import com.mansa.infrastructure.exception.StripeException;
import com.mansa.infrastructure.stripe.dto.StripePaymentRequest;
import com.mansa.infrastructure.stripe.dto.StripePaymentResponse;
/**
 * Handles complex Stripe workflows: 3DS, retries, partial captures, etc.
 */
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentConfirmParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
 
/**
 * Gère les workflows Stripe complexes :
 *   - Retry avec idempotency key
 *   - Confirmation après 3DS (requires_action → confirmed)
 *   - Capture différée
 */



@Slf4j
@Component
@RequiredArgsConstructor
public class StripePaymentProcessor {

    private final StripeClient stripeClient;
    private final StripeConfig stripeConfig;

    // ── Retry avec idempotency key ────────────────────────────────────────────

    /** Retourne uniquement le paymentIntentId (rétrocompatibilité). */
    public String authorizeWithRetry(StripePaymentRequest request, int maxRetries) {
        return authorizeWithRetryFull(request, maxRetries).getPaymentIntentId();
    }

    
    public StripePaymentResponse authorizeWithRetryFull(StripePaymentRequest request, int maxRetries) {
        String idempotencyKey = "auth-" + request.getCorrelationId();

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("Stripe authorize attempt {}/{} correlationId={}",
                        attempt, maxRetries, request.getCorrelationId());
                return stripeClient.createPaymentIntent(request);

            } catch (StripeException ex) {
                if (attempt == maxRetries) {
                    log.error("Stripe authorize failed after {} attempts: {}", maxRetries, ex.getMessage());
                    throw ex;
                }
                long backoffMs = 500L * attempt;
                log.warn("Stripe attempt {} failed, retrying in {}ms: {}", attempt, backoffMs, ex.getMessage());
                sleep(backoffMs);
            }
        }
        throw new StripeException("Max retries reached for correlationId=" + request.getCorrelationId(), null);
    }

    // ── Confirmation 3DS ──────────────────────────────────────────────────────

    public void confirm3ds(String paymentIntentId, String returnUrl) {
        log.info("Confirming 3DS for paymentIntentId={}", paymentIntentId);
        try {
            Stripe.apiKey = stripeConfig.getSecretKey();
            PaymentIntentConfirmParams params = PaymentIntentConfirmParams.builder()
                    .setReturnUrl(returnUrl != null ? returnUrl : stripeConfig.getReturnUrl())
                    .build();
            RequestOptions opts = stripeConfig.requestOptionsFor("3ds-confirm-" + paymentIntentId);
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
            intent.confirm(params, opts);
            log.info("3DS confirmed: paymentIntentId={} status={}", paymentIntentId, intent.getStatus());
        } catch (com.stripe.exception.StripeException ex) {
            throw new StripeException("3DS confirmation failed: " + ex.getMessage(), ex);
        }
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }
}




// @Slf4j
// @Component
// @RequiredArgsConstructor
// public class StripePaymentProcessor {
 
//     private final StripeClient  stripeClient;
//     private final StripeConfig  stripeConfig;
 
//     // ── Retry avec idempotency key ────────────────────────────────────────────
 
//     /**
//      * Tente l'autorisation jusqu'à {@code maxRetries} fois.
//      * Chaque tentative porte la même idempotency key → Stripe garantit
//      * qu'une seule charge est créée même si la requête est rejouée.
//      *
//      * @param request     données du paiement
//      * @param maxRetries  nombre max de tentatives (recommandé : 2-3)
//      * @return PaymentIntent ID Stripe
//      */
//     public String authorizeWithRetry(StripePaymentRequest request, int maxRetries) {
//         // L'idempotency key est fixe pour toute la séquence de retry
//         String idempotencyKey = "auth-" + request.getCorrelationId();
 
//         for (int attempt = 1; attempt <= maxRetries; attempt++) {
//             try {
//                 log.info("Stripe authorize attempt {}/{} correlationId={}",
//                         attempt, maxRetries, request.getCorrelationId());
//                 return stripeClient.createPaymentIntent(request).getPaymentIntentId();
 
//             } catch (StripeException ex) {
//                 boolean isLastAttempt = (attempt == maxRetries);
//                 if (isLastAttempt) {
//                     log.error("Stripe authorize failed after {} attempts: {}", maxRetries, ex.getMessage());
//                     throw ex;
//                 }
//                 long backoffMs = 500L * attempt;
//                 log.warn("Stripe attempt {} failed, retrying in {}ms: {}", attempt, backoffMs, ex.getMessage());
//                 sleep(backoffMs);
//             }
//         }
//         throw new StripeException("Max retries reached for correlationId=" + request.getCorrelationId(), null);
//     }
 
//     // ── Confirmation 3DS ──────────────────────────────────────────────────────
 
//     /**
//      * Appelé après que le porteur de carte a complété le challenge 3DS.
//      * Stripe envoie un webhook {@code payment_intent.requires_capture} ;
//      * le StripeWebhookController délègue ici.
//      *
//      * @param paymentIntentId  identifiant du PaymentIntent en attente
//      * @param returnUrl        URL de retour après 3DS (facultatif selon le flow)
//      */
//     public void confirm3ds(String paymentIntentId, String returnUrl) {
//         log.info("Confirming 3DS for paymentIntentId={}", paymentIntentId);
//         try {
//             Stripe.apiKey = stripeConfig.getSecretKey();
 
//             PaymentIntentConfirmParams params = PaymentIntentConfirmParams.builder()
//                     .setReturnUrl(returnUrl != null ? returnUrl : "https://bankpayx.com/payment/callback")
//                     .build();
 
//             RequestOptions opts = stripeConfig.buildRequestOptions("3ds-confirm-" + paymentIntentId);
//             PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
//             intent.confirm(params, opts);
 
//             log.info("3DS confirmed: paymentIntentId={} status={}", paymentIntentId, intent.getStatus());
 
//         } catch (com.stripe.exception.StripeException ex) {
//             log.error("3DS confirmation failed: paymentIntentId={} message={}", paymentIntentId, ex.getMessage());
//             throw new StripeException("3DS confirmation failed: " + ex.getMessage(), ex);
//         }
//     }
 
//     // ── Helpers ───────────────────────────────────────────────────────────────
 
//     private void sleep(long ms) {
//         try {
//             Thread.sleep(ms);
//         } catch (InterruptedException ie) {
//             Thread.currentThread().interrupt();
//         }
//     }
// }
 