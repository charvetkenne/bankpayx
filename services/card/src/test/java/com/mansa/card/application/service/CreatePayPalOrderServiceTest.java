package com.mansa.card.application.service;

import com.mansa.application.port.in.CreatePayPalOrderUseCase;
import com.mansa.application.port.out.EventPublisherPort;
import com.mansa.application.port.out.PayPalGatewayPort;
import com.mansa.application.port.out.PayPalOrderPersistencePort;
import com.mansa.application.service.CreatePayPalOrderService;
import com.mansa.domain.enums.PayPalOrderStatus;
import com.mansa.domain.model.PayPalOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreatePayPalOrderServiceTest {

    @Mock PayPalGatewayPort          gatewayPort;
    @Mock PayPalOrderPersistencePort persistencePort;
    @Mock EventPublisherPort         eventPublisherPort;

    CreatePayPalOrderService service;

    @BeforeEach
    void setUp() {
        service = new CreatePayPalOrderService(gatewayPort, persistencePort, eventPublisherPort);
    }

    private CreatePayPalOrderUseCase.Command buildCommand() {
        return new CreatePayPalOrderUseCase.Command(
                25.00, "EUR", "merchant_1", "Commande #42",
                "http://localhost:3000/success",
                "http://localhost:3000/cancel",
                "corr-pp-001"
        );
    }

    @Test
    void execute_successfulOrderCreation_returnsPendingApproval() {
        when(persistencePort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(gatewayPort.createOrder(anyLong(), any(), any(), any(), any(), any()))
                .thenReturn(new PayPalGatewayPort.OrderResult(
                        "ORDER-123",
                        "https://sandbox.paypal.com/checkoutnow?token=ORDER-123"
                ));

        PayPalOrder result = service.execute(buildCommand());

        assertThat(result.getStatus()).isEqualTo(PayPalOrderStatus.PENDING_APPROVAL);
        assertThat(result.getPaypalOrderId()).isEqualTo("ORDER-123");
        assertThat(result.getApproveUrl()).contains("ORDER-123");
        verify(eventPublisherPort).publishAll(any());
    }

    @Test
    void execute_gatewayThrows_returnsFailedOrder() {
        when(persistencePort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(gatewayPort.createOrder(anyLong(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("PayPal API error"));

        PayPalOrder result = service.execute(buildCommand());

        assertThat(result.getStatus()).isEqualTo(PayPalOrderStatus.FAILED);
        assertThat(result.getFailureReason()).contains("PayPal API error");
    }
}
