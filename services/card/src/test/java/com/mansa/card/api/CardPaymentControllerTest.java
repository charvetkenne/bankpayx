package com.mansa.card.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mansa.api.controller.CardPaymentController;
import com.mansa.api.dto.request.PaymentRequest;
import com.mansa.api.mapper.PaymentApiMapper;
import com.mansa.application.port.in.*;
import com.mansa.domain.enums.Currency;
import com.mansa.domain.enums.PaymentStatus;
import com.mansa.domain.model.CardPayment;
import com.mansa.domain.model.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardPaymentController.class)
@Import(PaymentApiMapper.class)
class CardPaymentControllerTest {

    @Autowired MockMvc      mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean ProcessCardPaymentUseCase processUseCase;
    @MockBean CapturePaymentUseCase     captureUseCase;
    @MockBean RefundPaymentUseCase      refundUseCase;
    @MockBean GetTransactionUseCase     getTransactionUseCase;

    private CardPayment buildFakePayment(PaymentStatus status) {
        return CardPayment.builder()
                .transactionId("txn-001")
                .amount(Money.of(50.00, Currency.EUR))
                .merchantId("merchant_1")
                .description("Test")
                .status(status)
                .gatewayTransactionId("pi_test_001")
                .correlationId("corr-001")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void processPayment_validRequest_returns201() throws Exception {
        when(processUseCase.process(any())).thenReturn(buildFakePayment(PaymentStatus.AUTHORIZED));

        PaymentRequest req = new PaymentRequest();
        req.setPaymentMethodId("pm_test_visa");
        req.setAmount(50.00);
        req.setCurrency("EUR");
        req.setMerchantId("merchant_1");
        req.setDescription("Test");

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AUTHORIZED"))
                .andExpect(jsonPath("$.transactionId").value("txn-001"));
    }

    @Test
    void processPayment_missingPaymentMethodId_returns400() throws Exception {
        PaymentRequest req = new PaymentRequest();
        req.setAmount(50.00);
        req.setCurrency("EUR");
        req.setMerchantId("merchant_1");
        // paymentMethodId manquant → 400

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void processPayment_invalidCurrency_returns400() throws Exception {
        PaymentRequest req = new PaymentRequest();
        req.setPaymentMethodId("pm_test");
        req.setAmount(50.00);
        req.setCurrency("XOF"); // non supporté par pattern
        req.setMerchantId("merchant_1");

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTransaction_existingId_returns200() throws Exception {
        when(getTransactionUseCase.getByTransactionId("txn-001"))
                .thenReturn(buildFakePayment(PaymentStatus.CAPTURED));

        mockMvc.perform(get("/api/v1/payments/txn-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("txn-001"))
                .andExpect(jsonPath("$.status").value("CAPTURED"));
    }
}
