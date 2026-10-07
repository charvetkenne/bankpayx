package com.mansa.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;





@Getter
@Builder
public class PaymentResponse {

    // ── Transaction ───────────────────────────────────────────────────────────
    private String     transactionId;
    private String     status;
    private String     maskedCardNumber;
    private String     cardHolder;
    private String     cardType;
    private BigDecimal amount;
    private String     currency;
    private String     merchantId;
    private String     description;
    private String     gatewayTransactionId;
    private String     failureReason;
    private Instant    createdAt;
    private Instant    updatedAt;
    private String     correlationId;

    // ── Actions Stripe supplémentaires ────────────────────────────────────────
    private boolean requiresAction;  // → frontend doit appeler stripe.handleNextAction()
    private String  clientSecret;    // → passé à stripe.handleNextAction(clientSecret)
    private String  redirectUrl;     // → redirection externe (PayPal, Bancontact…)
}





// @Getter
// @Builder
// public class PaymentResponse {
//     private String     transactionId;
//     private String     status;
//     private String     maskedCardNumber;   // ex: **** **** **** 4242 (depuis Stripe)
//     private String     cardHolder;         // nom du porteur (depuis Stripe si disponible)
//     private String     cardType;           // VISA, MASTERCARD… (depuis Stripe)
//     private BigDecimal amount;
//     private String     currency;
//     private String     merchantId;
//     private String     description;
//     private String     gatewayTransactionId;
//     private String     failureReason;
//     private Instant    createdAt;
//     private Instant    updatedAt;
//     private String     correlationId;
// }

