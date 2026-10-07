package com.mansa.infrastructure.paypal.persistence.mapper;

import com.mansa.domain.model.Money;
import com.mansa.domain.model.PayPalOrder;
import com.mansa.infrastructure.paypal.persistence.entity.PayPalOrderEntity;
import org.springframework.stereotype.Component;

@Component
public class PayPalOrderMapper {

    public PayPalOrderEntity toEntity(PayPalOrder order) {
        return PayPalOrderEntity.builder()
                .transactionId(order.getTransactionId())
                .paypalOrderId(order.getPaypalOrderId())
                .captureId(order.getCaptureId())
                .amount(order.getAmount().getAmount())
                .currency(order.getAmount().getCurrency())
                .merchantId(order.getMerchantId())
                .description(order.getDescription())
                .returnUrl(order.getReturnUrl())
                .cancelUrl(order.getCancelUrl())
                .approveUrl(order.getApproveUrl())
                .correlationId(order.getCorrelationId())
                .failureReason(order.getFailureReason())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public PayPalOrder toDomain(PayPalOrderEntity e) {
        Money money = Money.of(e.getAmount(), e.getCurrency());
        return PayPalOrder.builder()
                .transactionId(e.getTransactionId())
                .paypalOrderId(e.getPaypalOrderId())
                .captureId(e.getCaptureId())
                .amount(money)
                .merchantId(e.getMerchantId())
                .description(e.getDescription())
                .returnUrl(e.getReturnUrl())
                .cancelUrl(e.getCancelUrl())
                .approveUrl(e.getApproveUrl())
                .correlationId(e.getCorrelationId())
                .failureReason(e.getFailureReason())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
