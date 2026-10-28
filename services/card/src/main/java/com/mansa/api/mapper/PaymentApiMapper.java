package com.mansa.api.mapper;

import com.mansa.api.dto.response.PaymentResponse;
import com.mansa.domain.model.CardPayment;
import org.springframework.stereotype.Component;





@Component
public class PaymentApiMapper {

    public PaymentResponse toResponse(CardPayment payment) {
        return PaymentResponse.builder()
                .transactionId(payment.getTransactionId())
                .status(payment.getStatus().name())
                .maskedCardNumber(payment.getMaskedCardNumber() != null
                        ? payment.getMaskedCardNumber() : "****")
                .cardType(payment.getCardType() != null
                        ? payment.getCardType().name() : "UNKNOWN")
                .cardHolder(payment.getCardHolder())
                .amount(payment.getAmount().getAmount())
                .currency(payment.getAmount().getCurrency().name())
                .merchantId(payment.getMerchantId())
                .description(payment.getDescription())
                .gatewayTransactionId(payment.getGatewayTransactionId())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .correlationId(payment.getCorrelationId())
                // Champs d'action supplémentaire pour le frontend
                .requiresAction(payment.isRequiresAction())
                .clientSecret(payment.getClientSecret())
                .redirectUrl(payment.getRedirectUrl())
                .build();
    }
}





// @Component
// public class PaymentApiMapper {

//     public PaymentResponse toResponse(CardPayment payment) {
//         return PaymentResponse.builder()
//                 .transactionId(payment.getTransactionId())
//                 .status(payment.getStatus().name())
//                 // maskedCardNumber et cardType sont nullables (peuplés après réponse Stripe)
//                 .maskedCardNumber(payment.getMaskedCardNumber() != null
//                         ? payment.getMaskedCardNumber() : "****")
//                 .cardType(payment.getCardType() != null
//                         ? payment.getCardType().name() : "UNKNOWN")
//                 .cardHolder(payment.getCardHolder())
//                 .amount(payment.getAmount().getAmount())
//                 .currency(payment.getAmount().getCurrency().name())
//                 .merchantId(payment.getMerchantId())
//                 .description(payment.getDescription())
//                 .gatewayTransactionId(payment.getGatewayTransactionId())
//                 .failureReason(payment.getFailureReason())
//                 .createdAt(payment.getCreatedAt())
//                 .updatedAt(payment.getUpdatedAt())
//                 .correlationId(payment.getCorrelationId())
//                 .build();
//     }
// }
