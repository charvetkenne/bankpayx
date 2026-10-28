package com.mansa.card.application;

import com.mansa.application.port.in.CapturePayPalOrderUseCase;
import com.mansa.application.port.out.EventPublisherPort;
import com.mansa.application.port.out.PayPalGatewayPort;
import com.mansa.application.port.out.PayPalOrderPersistencePort;
import com.mansa.application.service.CapturePayPalOrderService;
import com.mansa.domain.enums.Currency;
import com.mansa.domain.enums.PayPalOrderStatus;
import com.mansa.domain.exception.PaymentNotFoundException;
import com.mansa.domain.model.Money;
import com.mansa.domain.model.PayPalOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CapturePayPalOrderServiceTest {

    @Mock PayPalGatewayPort          gatewayPort;
    @Mock PayPalOrderPersistencePort persistencePort;
    @Mock EventPublisherPort         eventPublisherPort;

    CapturePayPalOrderService service;

    @BeforeEach
    void setUp() {
        service = new CapturePayPalOrderService(gatewayPort, persistencePort, eventPublisherPort);
    }

    private PayPalOrder buildPendingOrder() {
        PayPalOrder order = PayPalOrder.initiate(
                Money.of(25.00, Currency.EUR), "merchant_1", "Commande",
                "http://return", "http://cancel", "corr-001"
        );
        order.markCreated("ORDER-123", "https://sandbox.paypal.com/checkoutnow?token=ORDER-123");
        order.pullDomainEvents(); // vider
        return order;
    }

    @Test
    void execute_successfulCapture_returnsCapturedOrder() {
        PayPalOrder order = buildPendingOrder();
        when(persistencePort.findByPaypalOrderId("ORDER-123")).thenReturn(Optional.of(order));
        when(persistencePort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(gatewayPort.captureOrder("ORDER-123")).thenReturn("CAPTURE-999");

        PayPalOrder result = service.execute(
                new CapturePayPalOrderUseCase.Command("ORDER-123", "corr-001"));

        assertThat(result.getStatus()).isEqualTo(PayPalOrderStatus.CAPTURED);
        assertThat(result.getCaptureId()).isEqualTo("CAPTURE-999");
        verify(eventPublisherPort).publishAll(any());
    }

    @Test
    void execute_orderNotFound_throwsException() {
        when(persistencePort.findByPaypalOrderId("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(
                new CapturePayPalOrderUseCase.Command("UNKNOWN", "corr-001")))
                .isInstanceOf(PaymentNotFoundException.class);
    }
}
