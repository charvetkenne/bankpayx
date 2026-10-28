// package com.mansa.card.api;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.mansa.api.controller.PayPalPaymentController;
// import com.mansa.api.dto.request.CapturePayPalOrderRequest;
// import com.mansa.api.dto.request.CreatePayPalOrderRequest;
// import com.mansa.application.port.in.CapturePayPalOrderUseCase;
// import com.mansa.application.port.in.CreatePayPalOrderUseCase;
// import com.mansa.domain.enums.Currency;
// import com.mansa.domain.enums.PayPalOrderStatus;
// import com.mansa.domain.model.Money;
// import com.mansa.domain.model.PayPalOrder;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.http.MediaType;
// import org.springframework.test.web.servlet.MockMvc;

// import java.time.Instant;

// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.when;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest(PayPalPaymentController.class)
// class PayPalPaymentControllerTest {

//     @Autowired MockMvc      mockMvc;
//     @Autowired ObjectMapper objectMapper;

//     @MockBean CreatePayPalOrderUseCase  createUseCase;
//     @MockBean CapturePayPalOrderUseCase captureUseCase;

//     private PayPalOrder buildFakeOrder(PayPalOrderStatus status) {
//         return PayPalOrder.builder()
//                 .transactionId("txn-pp-001")
//                 .paypalOrderId("ORDER-123")
//                 .amount(Money.of(25.00, Currency.EUR))
//                 .merchantId("merchant_1")
//                 .description("Commande #42")
//                 .status(status)
//                 .approveUrl("https://sandbox.paypal.com/checkoutnow?token=ORDER-123")
//                 .returnUrl("http://localhost:3000/success")
//                 .cancelUrl("http://localhost:3000/cancel")
//                 .correlationId("corr-pp-001")
//                 .createdAt(Instant.now())
//                 .updatedAt(Instant.now())
//                 .build();
//     }

//     @Test
//     void createOrder_validRequest_returns201WithApproveUrl() throws Exception {
//         when(createUseCase.execute(any()))
//                 .thenReturn(buildFakeOrder(PayPalOrderStatus.PENDING_APPROVAL));

//         CreatePayPalOrderRequest req = new CreatePayPalOrderRequest();
//         req.setAmount(25.00);
//         req.setCurrency("EUR");
//         req.setMerchantId("merchant_1");
//         req.setDescription("Commande #42");
//         req.setReturnUrl("http://localhost:3000/success");
//         req.setCancelUrl("http://localhost:3000/cancel");

//         mockMvc.perform(post("/api/v1/paypal/create-order")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(req)))
//                 .andExpect(status().isCreated())
//                 .andExpect(jsonPath("$.orderId").value("ORDER-123"))
//                 .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"))
//                 .andExpect(jsonPath("$.approveUrl").exists());
//     }

//     @Test
//     void createOrder_xofCurrency_returns400() throws Exception {
//         CreatePayPalOrderRequest req = new CreatePayPalOrderRequest();
//         req.setAmount(25.00);
//         req.setCurrency("XOF"); // non supporté PayPal
//         req.setMerchantId("merchant_1");
//         req.setReturnUrl("http://localhost:3000/success");
//         req.setCancelUrl("http://localhost:3000/cancel");

//         mockMvc.perform(post("/api/v1/paypal/create-order")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(req)))
//                 .andExpect(status().isBadRequest());
//     }

//     @Test
//     void captureOrder_validOrderId_returns200() throws Exception {
//         when(captureUseCase.execute(any()))
//                 .thenReturn(buildFakeOrder(PayPalOrderStatus.CAPTURED));

//         CapturePayPalOrderRequest req = new CapturePayPalOrderRequest();
//         req.setOrderId("ORDER-123");

//         mockMvc.perform(post("/api/v1/paypal/capture-order")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(req)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.status").value("CAPTURED"))
//                 .andExpect(jsonPath("$.orderId").value("ORDER-123"));
//     }

//     @Test
//     void captureOrder_missingOrderId_returns400() throws Exception {
//         CapturePayPalOrderRequest req = new CapturePayPalOrderRequest();
//         // orderId absent

//         mockMvc.perform(post("/api/v1/paypal/capture-order")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(req)))
//                 .andExpect(status().isBadRequest());
//     }
// }
