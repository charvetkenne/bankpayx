package com.mansa.card.application.service;

import com.mansa.application.port.out.PayPalOrderPersistencePort;
import com.mansa.application.port.in.ProcessCardPaymentUseCase;
import com.mansa.application.port.out.EventPublisherPort;
import com.mansa.application.port.out.PaymentGatewayPort;
import com.mansa.application.port.out.TransactionPersistencePort;
import com.mansa.application.service.CardPaymentService;
import com.mansa.domain.enums.PaymentStatus;
import com.mansa.domain.model.CardPayment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardPaymentServiceTest {

    @Mock PaymentGatewayPort         gatewayPort;
    @Mock TransactionPersistencePort persistencePort;
    @Mock EventPublisherPort         eventPublisherPort;

    CardPaymentService service;

    @BeforeEach
    void setUp() {
        service = new CardPaymentService(gatewayPort, persistencePort, eventPublisherPort);
    }

    private ProcessCardPaymentUseCase.Command buildCommand() {
        return new ProcessCardPaymentUseCase.Command(
                "pm_test_visa", 50.00, "EUR", "merchant_1", "Test", "corr-001");
    }

    @Test
    void process_successfulAuthorization_returnsAuthorizedPayment() {
        
        when(persistencePort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(gatewayPort.authorize(any())).thenReturn("pi_test_001");

        CardPayment result = service.process(buildCommand());

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
        assertThat(result.getGatewayTransactionId()).isEqualTo("pi_test_001");
        verify(persistencePort, times(2)).save(any());
        verify(eventPublisherPort).publishAll(any());
    }

    @Test
    void process_gatewayThrows_returnsFailedPayment() {
        when(persistencePort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(gatewayPort.authorize(any())).thenThrow(new RuntimeException("Stripe down"));

        CardPayment result = service.process(buildCommand());

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(result.getFailureReason()).contains("Stripe down");
        verify(eventPublisherPort).publishAll(any());
    }

    @Test
    void process_persistsBeforeGatewayCall() {
        when(persistencePort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(gatewayPort.authorize(any())).thenReturn("pi_001");

        service.process(buildCommand());

        // Vérifie l'ordre : persist PENDING → authorize → persist AUTHORIZED
        var inOrder = inOrder(persistencePort, gatewayPort);
        inOrder.verify(persistencePort).save(any());
        inOrder.verify(gatewayPort).authorize(any());
        inOrder.verify(persistencePort).save(any());
    }
}
