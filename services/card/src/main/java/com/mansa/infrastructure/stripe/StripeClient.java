package com.mansa.infrastructure.stripe;

import com.mansa.infrastructure.exception.StripeException;
import com.mansa.infrastructure.stripe.dto.StripePaymentRequest;
import com.mansa.infrastructure.stripe.dto.StripePaymentResponse;
//import com.mansa.infrastructure.stripe.StripeConfig;
//import javax.smartcardio.CardException;
//import org.springframework.web.client.RestTemplate;


import com.stripe.Stripe;
import com.stripe.exception.CardException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCaptureParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * Couche technique bas niveau : appels directs au SDK Stripe.
 *
 * Responsabilité unique = dialoguer avec l'API Stripe.
 * Toute orchestration métier (retry, 3DS, capture différée)
 * est déléguée à StripePaymentProcessor ou StripeAdapter.
 *
 * NOTE : Stripe.apiKey est positionné avant chaque appel pour
 * permettre un rechargement à chaud sans redémarrage.
 * En production multi-tenant, remplacer par RequestOptions.
 */



@Slf4j
@Component
@RequiredArgsConstructor
public class StripeClient {

    private final StripeConfig stripeConfig;

    // ── Authorize ─────────────────────────────────────────────────────────────

    public StripePaymentResponse createPaymentIntent(StripePaymentRequest request) {
        log.debug("Stripe createPaymentIntent: amount={} currency={}",
                request.getAmountInCents(), request.getCurrency());
        try {
            Stripe.apiKey = stripeConfig.getSecretKey();

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(request.getAmountInCents())
                    .setCurrency(request.getCurrency())
                    .setDescription(request.getDescription())
                    .setPaymentMethod(request.getPaymentMethodId())
                    .setConfirm(true)
                    .setConfirmationMethod(PaymentIntentCreateParams.ConfirmationMethod.MANUAL)
                    .putMetadata("merchantId",    request.getMerchantId())
                    .putMetadata("correlationId", request.getCorrelationId())
                    /*
                     * return_url obligatoire quand :
                     *   - confirm = true côté serveur
                     *   - des méthodes de paiement avec redirection sont activées
                     *     dans le Dashboard Stripe (PayPal, Bancontact, iDEAL, Klarna…)
                     *
                     * Stripe redirige l'utilisateur vers cette URL après qu'il a
                     * complété l'action externe (3DS, approbation PayPal, etc.).
                     * La page /payment/callback côté frontend lit ensuite le résultat.
                     */
                    .setReturnUrl(stripeConfig.getReturnUrl())
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            log.info("Stripe PaymentIntent created: id={} status={}", intent.getId(), intent.getStatus());

            // Extraire l'URL de redirection si Stripe en retourne une
            // (cas des méthodes PayPal, Bancontact, iDEAL…)
            String redirectUrl = null;
            if (intent.getNextAction() != null
                    && intent.getNextAction().getRedirectToUrl() != null) {
                redirectUrl = intent.getNextAction().getRedirectToUrl().getUrl();
                log.info("Stripe requires redirect: url={}", redirectUrl);
            }

            return StripePaymentResponse.builder()
                    .paymentIntentId(intent.getId())
                    .status(intent.getStatus())
                    .requiresAction("requires_action".equals(intent.getStatus()))
                    .clientSecret(intent.getClientSecret())
                    .redirectUrl(redirectUrl)
                    .build();

        } catch (CardException ex) {
            log.warn("Stripe card declined: code={} declineCode={} message={}",
                    ex.getCode(), ex.getDeclineCode(), ex.getMessage());
            throw new StripeException("Card declined [" + ex.getDeclineCode() + "]: " + ex.getMessage(), ex);

        } catch (com.stripe.exception.StripeException ex) {
            log.error("Stripe API error – type={} requestId={} message={}",
                    ex.getClass().getSimpleName(), ex.getRequestId(), ex.getMessage());
            throw new StripeException("Stripe authorization failed: " + ex.getMessage(), ex);
        }
    }

    // ── Capture ───────────────────────────────────────────────────────────────

    public void capturePaymentIntent(String paymentIntentId) {
        log.debug("Stripe capture: paymentIntentId={}", paymentIntentId);
        try {
            Stripe.apiKey = stripeConfig.getSecretKey();
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
            intent.capture(PaymentIntentCaptureParams.builder().build());
            log.info("Stripe capture OK: paymentIntentId={} status={}", paymentIntentId, intent.getStatus());
        } catch (com.stripe.exception.StripeException ex) {
            log.error("Stripe capture failed: paymentIntentId={} requestId={} message={}",
                    paymentIntentId, ex.getRequestId(), ex.getMessage());
            throw new StripeException("Stripe capture failed: " + ex.getMessage(), ex);
        }
    }

    // ── Refund ────────────────────────────────────────────────────────────────

    public void refundPaymentIntent(String paymentIntentId) {
        log.debug("Stripe refund: paymentIntentId={}", paymentIntentId);
        try {
            Stripe.apiKey = stripeConfig.getSecretKey();
            Refund refund = Refund.create(
                    RefundCreateParams.builder().setPaymentIntent(paymentIntentId).build());
            log.info("Stripe refund OK: refundId={} status={}", refund.getId(), refund.getStatus());
        } catch (com.stripe.exception.StripeException ex) {
            log.error("Stripe refund failed: paymentIntentId={} requestId={} message={}",
                    paymentIntentId, ex.getRequestId(), ex.getMessage());
            throw new StripeException("Stripe refund failed: " + ex.getMessage(), ex);
        }
    }
}



// @Slf4j
// @Component
// @RequiredArgsConstructor
// public class StripeClient {

//     private final StripeConfig stripeConfig;

//     // ── Authorize ─────────────────────────────────────────────────────────────

//     /**
//      * Crée un PaymentIntent Stripe en mode MANUAL (authorize only).
//      * La capture est déclenchée séparément via capturePaymentIntent().
//      */
//     public StripePaymentResponse createPaymentIntent(StripePaymentRequest request) {
//         log.debug("Stripe createPaymentIntent: amount={} currency={}",
//                 request.getAmountInCents(), request.getCurrency());
//         try {
//             Stripe.apiKey = stripeConfig.getSecretKey();

//             PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
//                     .setAmount(request.getAmountInCents())
//                     .setCurrency(request.getCurrency())
//                     .setDescription(request.getDescription())
//                     .setPaymentMethod(request.getPaymentMethodId())
//                     .setConfirm(true)
//                     .setConfirmationMethod(PaymentIntentCreateParams.ConfirmationMethod.MANUAL)
//                     .putMetadata("merchantId",    request.getMerchantId())
//                     .putMetadata("correlationId", request.getCorrelationId())
//                     .build();

//             PaymentIntent intent = PaymentIntent.create(params);

//             log.info("Stripe PaymentIntent created: id={} status={}", intent.getId(), intent.getStatus());

//             return StripePaymentResponse.builder()
//                     .paymentIntentId(intent.getId())
//                     .status(intent.getStatus())
//                     .requiresAction("requires_action".equals(intent.getStatus()))
//                     .clientSecret(intent.getClientSecret())
//                     .build();

//         } catch (CardException ex) {
//             // La carte a été refusée par le réseau carte (fonds insuffisants, carte volée…)
//             log.warn("Stripe card declined: code={} declineCode={} message={}",
//                     ex.getCode(), ex.getDeclineCode(), ex.getMessage());
//             throw new StripeException("Card declined [" + ex.getDeclineCode() + "]: " + ex.getMessage(), ex);

//         } catch (com.stripe.exception.StripeException ex) {
//             // Toute autre erreur API Stripe (auth, rate-limit, réseau…)
//             log.error("Stripe API error – type={} requestId={} message={}",
//                     ex.getClass().getSimpleName(), ex.getRequestId(), ex.getMessage());
//             throw new StripeException("Stripe authorization failed: " + ex.getMessage(), ex);
//         }
//     }

//     // ── Capture ───────────────────────────────────────────────────────────────

//     /**
//      * Capture un PaymentIntent préalablement autorisé (status = requires_capture).
//      */
//     public void capturePaymentIntent(String paymentIntentId) {
//         log.debug("Stripe capture: paymentIntentId={}", paymentIntentId);
//         try {
//             Stripe.apiKey = stripeConfig.getSecretKey();

//             PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
//             intent.capture(PaymentIntentCaptureParams.builder().build());

//             log.info("Stripe capture OK: paymentIntentId={} status={}", paymentIntentId, intent.getStatus());

//         } catch (com.stripe.exception.StripeException ex) {
//             log.error("Stripe capture failed: paymentIntentId={} requestId={} message={}",
//                     paymentIntentId, ex.getRequestId(), ex.getMessage());
//             throw new StripeException("Stripe capture failed: " + ex.getMessage(), ex);
//         }
//     }

//     // ── Refund ────────────────────────────────────────────────────────────────

//     /**
//      * Émet un remboursement total sur un PaymentIntent capturé.
//      * Pour un remboursement partiel, ajouter .setAmount(amountInCents) aux params.
//      */
//     public void refundPaymentIntent(String paymentIntentId) {
//         log.debug("Stripe refund: paymentIntentId={}", paymentIntentId);
//         try {
//             Stripe.apiKey = stripeConfig.getSecretKey();

//             RefundCreateParams params = RefundCreateParams.builder()
//                     .setPaymentIntent(paymentIntentId)
//                     .build();

//             Refund refund = Refund.create(params);

//             log.info("Stripe refund OK: refundId={} status={}", refund.getId(), refund.getStatus());

//         } catch (com.stripe.exception.StripeException ex) {
//             log.error("Stripe refund failed: paymentIntentId={} requestId={} message={}",
//                     paymentIntentId, ex.getRequestId(), ex.getMessage());
//             throw new StripeException("Stripe refund failed: " + ex.getMessage(), ex);
//         }
//     }
// }