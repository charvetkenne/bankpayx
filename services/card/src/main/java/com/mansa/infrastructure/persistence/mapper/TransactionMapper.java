package com.mansa.infrastructure.persistence.mapper;

//import com.mansa.domain.model.Card;
import com.mansa.domain.model.CardPayment;
import com.mansa.domain.model.Money;
import com.mansa.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.stereotype.Component;

//import java.time.YearMonth;

@Component
public class TransactionMapper {

    public TransactionEntity toEntity(CardPayment payment) {
        return TransactionEntity.builder()
                .transactionId(payment.getTransactionId())
                // Données carte — nullables si Stripe n'a pas encore répondu
                .maskedCardNumber(payment.getMaskedCardNumber())
                .cardHolder(payment.getCardHolder())
                .cardType(payment.getCardType())
                // On ne stocke plus expiryMonth/expiryYear : non disponibles sans données brutes
                // Ils peuvent être récupérés ultérieurement via Stripe PaymentMethod API
                .amount(payment.getAmount().getAmount())
                .currency(payment.getAmount().getCurrency())
                .merchantId(payment.getMerchantId())
                .description(payment.getDescription())
                .correlationId(payment.getCorrelationId())
                .gatewayPaymentMethodId(payment.getGatewayPaymentMethodId())
                .gatewayTransactionId(payment.getGatewayTransactionId())
                .failureReason(payment.getFailureReason())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    public CardPayment toDomain(TransactionEntity e) {
        Money money = Money.of(e.getAmount(), e.getCurrency());

        return CardPayment.builder()
                .transactionId(e.getTransactionId())
                .maskedCardNumber(e.getMaskedCardNumber())
                .cardHolder(e.getCardHolder())
                .cardType(e.getCardType())
                .amount(money)
                .merchantId(e.getMerchantId())
                .description(e.getDescription())
                .correlationId(e.getCorrelationId())
                .gatewayPaymentMethodId(e.getGatewayPaymentMethodId())
                .gatewayTransactionId(e.getGatewayTransactionId())
                .failureReason(e.getFailureReason())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
