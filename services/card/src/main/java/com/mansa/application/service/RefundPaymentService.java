package com.mansa.application.service;

import com.mansa.application.port.in.RefundPaymentUseCase;
import com.mansa.application.port.out.EventPublisherPort;
import com.mansa.application.port.out.PaymentGatewayPort;
import com.mansa.application.port.out.TransactionPersistencePort;
import com.mansa.domain.enums.PaymentStatus;
import com.mansa.domain.exception.PaymentNotFoundException;
import com.mansa.domain.model.CardPayment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundPaymentService implements RefundPaymentUseCase {

    private final PaymentGatewayPort         gatewayPort;
    private final TransactionPersistencePort persistencePort;
    private final EventPublisherPort         eventPublisherPort;

    @Override
    @Transactional
    public CardPayment refund(String transactionId) {
        log.info("Refunding payment transactionId={}", transactionId);

        CardPayment payment = persistencePort.findByTransactionId(transactionId)
                .orElseThrow(() -> new PaymentNotFoundException(transactionId));

        if (payment.getStatus() != PaymentStatus.CAPTURED)
            throw new IllegalStateException("Payment must be CAPTURED to refund. Current status: " + payment.getStatus());

        gatewayPort.refund(payment.getGatewayTransactionId());
        payment.markRefunded();

        CardPayment updated = persistencePort.save(payment);
        eventPublisherPort.publishAll(updated.pullDomainEvents());
        return updated;
    }
}
