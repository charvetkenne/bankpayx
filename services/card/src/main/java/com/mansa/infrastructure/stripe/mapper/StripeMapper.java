package com.mansa.infrastructure.stripe.mapper;

import com.mansa.domain.model.CardPayment;
import com.mansa.infrastructure.stripe.dto.StripePaymentRequest;
import org.springframework.stereotype.Component;

@Component
public class StripeMapper {

    public StripePaymentRequest toStripeRequest(CardPayment payment) {
        return StripePaymentRequest.builder()
                .amountInCents(payment.getAmount().toSmallestUnit())
                .currency(payment.getAmount().getCurrency().name().toLowerCase())
                .description(payment.getDescription())
                .merchantId(payment.getMerchantId())
                .correlationId(payment.getCorrelationId())
                .paymentMethodId(payment.getGatewayPaymentMethodId()) // pm_xxx réel du frontend
                .build();
    }
}
