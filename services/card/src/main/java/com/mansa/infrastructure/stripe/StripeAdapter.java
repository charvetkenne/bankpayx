package com.mansa.infrastructure.stripe;

import com.mansa.application.port.out.PaymentGatewayPort;
import com.mansa.domain.model.CardPayment;
import com.mansa.infrastructure.stripe.dto.StripePaymentRequest;
import com.mansa.infrastructure.stripe.dto.StripePaymentResponse;
//import com.mansa.infrastructure.stripe.dto.StripePaymentResponse;
import com.mansa.infrastructure.stripe.mapper.StripeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StripeAdapter implements PaymentGatewayPort {

    private static final int MAX_RETRIES = 2;

    private final StripePaymentProcessor paymentProcessor;
    private final StripeClient           stripeClient;
    private final StripeMapper           stripeMapper;

    /**
     * Autorise un paiement via Stripe.
     *
     * Retourne le PaymentIntent ID (pi_xxx).
     * Si requires_action, le PaymentIntent ID est quand même retourné —
     * c'est CardPaymentService qui appellera markRequiresAction() via
     * les données portées par la réponse étendue.
     */
    @Override
    public String authorize(CardPayment payment) {

        System.out.println("STRIPE ADAPTER START");
        StripePaymentRequest  request  = stripeMapper.toStripeRequest(payment);

        System.out.println("CALLING STRIPE...");

        StripePaymentResponse response = paymentProcessor.authorizeWithRetryFull(request, MAX_RETRIES);
        System.out.println("STRIPE RESPONSE RECEIVED");

        log.info("[{}] Stripe response: id={} status={} requiresAction={}",
                payment.getCorrelationId(),
                response.getPaymentIntentId(), response.getStatus(), response.isRequiresAction());

        // Si le paiement nécessite une action supplémentaire,
        // mettre à jour le domaine avec les informations nécessaires au frontend
        if (response.isRequiresAction()) {
            payment.markRequiresAction(
                    response.getPaymentIntentId(),
                    response.getClientSecret(),
                    response.getRedirectUrl()
            );
        }

        return response.getPaymentIntentId();
    }

    @Override
    public void capture(String gatewayTransactionId) {
        stripeClient.capturePaymentIntent(gatewayTransactionId);
        log.info("Stripe capture OK: paymentIntentId={}", gatewayTransactionId);
    }

    @Override
    public void refund(String gatewayTransactionId) {
        stripeClient.refundPaymentIntent(gatewayTransactionId);
        log.info("Stripe refund OK: paymentIntentId={}", gatewayTransactionId);
    }
}







// public class StripeAdapter implements PaymentGatewayPort {

    
//             private static final int MAX_RETRIES = 2;
         
//             private final StripePaymentProcessor paymentProcessor;
//             private final StripeClient           stripeClient;
//             private final StripeMapper           stripeMapper;
         
//             @Override
//             public String authorize(CardPayment payment) {
//                 StripePaymentRequest request = stripeMapper.toStripeRequest(payment);
//                 // Utilise le retry avec idempotency key pour éviter les doubles débits
//                 String paymentIntentId = paymentProcessor.authorizeWithRetry(request, MAX_RETRIES);
//                 log.info("[{}] Stripe authorization OK: paymentIntentId={}",
//                         payment.getCorrelationId(), paymentIntentId);
//                 return paymentIntentId;
//             }
        
//     @Override
//     public void capture(String gatewayTransactionId) {
//         stripeClient.capturePaymentIntent(gatewayTransactionId);
//         log.info("Stripe capture OK: paymentIntentId={}", gatewayTransactionId);
//     }

//     @Override
//     public void refund(String gatewayTransactionId) {
//         stripeClient.refundPaymentIntent(gatewayTransactionId);
//         log.info("Stripe refund OK: paymentIntentId={}", gatewayTransactionId);
//     }
// }
