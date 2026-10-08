package com.mansa.application.service;

import com.mansa.application.port.in.CapturePayPalOrderUseCase;
import com.mansa.application.port.out.EventPublisherPort;
import com.mansa.application.port.out.PayPalGatewayPort;
import com.mansa.application.port.out.PayPalOrderPersistencePort;
import com.mansa.domain.enums.PayPalOrderStatus;
import com.mansa.domain.exception.PaymentNotFoundException;
import com.mansa.domain.model.PayPalOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CapturePayPalOrderService implements CapturePayPalOrderUseCase {

    private final PayPalGatewayPort          gatewayPort;
    private final PayPalOrderPersistencePort persistencePort;
    private final EventPublisherPort         eventPublisherPort;

    @Override
    @Transactional
    public PayPalOrder execute(Command command) {
        log.info("[{}] Capturing PayPal order: orderId={}", command.correlationId(), command.orderId());

        PayPalOrder order = persistencePort.findByPaypalOrderId(command.orderId())
                .orElseThrow(() -> new PaymentNotFoundException("PayPal order not found: " + command.orderId()));

        if (order.getStatus() == PayPalOrderStatus.CAPTURED) {
            log.warn("PayPal order already captured: orderId={}", command.orderId());
            return order;
        }

        try {
            String captureId = gatewayPort.captureOrder(command.orderId());
            order.markCaptured(captureId);
            log.info("[{}] PayPal order captured: orderId={} captureId={}",
                    command.correlationId(), command.orderId(), captureId);

        } catch (Exception ex) {
            log.error("[{}] PayPal capture failed: orderId={} reason={}",
                    command.correlationId(), command.orderId(), ex.getMessage());
            order.markFailed(ex.getMessage());
        }

        PayPalOrder updated = persistencePort.save(order);
        eventPublisherPort.publishAll(updated.pullDomainEvents());
        return updated;
    }
}
