package com.mansa.application.service;

import com.mansa.application.port.in.CreatePayPalOrderUseCase;
import com.mansa.application.port.out.EventPublisherPort;
import com.mansa.application.port.out.PayPalGatewayPort;
import com.mansa.application.port.out.PayPalOrderPersistencePort;
import com.mansa.domain.enums.Currency;
import com.mansa.domain.model.Money;
import com.mansa.domain.model.PayPalOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreatePayPalOrderService implements CreatePayPalOrderUseCase {

    private final PayPalGatewayPort           gatewayPort;
    private final PayPalOrderPersistencePort  persistencePort;
    private final EventPublisherPort          eventPublisherPort;

    @Override
    @Transactional
    public PayPalOrder execute(Command command) {
        log.info("[{}] Creating PayPal order: merchant={} amount={} {}",
                command.correlationId(), command.merchantId(),
                command.amount(), command.currency());

        Money money = Money.of(command.amount(), Currency.valueOf(command.currency()));

        PayPalOrder order = PayPalOrder.initiate(
                money, command.merchantId(), command.description(),
                command.returnUrl(), command.cancelUrl(), command.correlationId()
        );

        // Persister en PENDING_APPROVAL avant l'appel PayPal
        PayPalOrder saved = persistencePort.save(order);

        try {
            PayPalGatewayPort.OrderResult result = gatewayPort.createOrder(
                    money.toSmallestUnit(),
                    command.currency().toLowerCase(),
                    command.description(),
                    command.returnUrl(),
                    command.cancelUrl(),
                    command.correlationId()
            );

            saved.markCreated(result.orderId(), result.approveUrl());
            log.info("[{}] PayPal order created: orderId={} approveUrl={}",
                    command.correlationId(), result.orderId(), result.approveUrl());

        } catch (Exception ex) {
            log.error("[{}] PayPal order creation failed: {}", command.correlationId(), ex.getMessage());
            saved.markFailed(ex.getMessage());
        }

        PayPalOrder updated = persistencePort.save(saved);
        eventPublisherPort.publishAll(updated.pullDomainEvents());
        return updated;
    }
}
