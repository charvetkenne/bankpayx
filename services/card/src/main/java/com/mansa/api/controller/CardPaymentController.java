package com.mansa.api.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mansa.api.dto.request.PaymentRequest;
import com.mansa.api.dto.response.PaymentResponse;
import com.mansa.api.mapper.PaymentApiMapper;
import com.mansa.application.port.in.CapturePaymentUseCase;
import com.mansa.application.port.in.GetTransactionUseCase;
import com.mansa.application.port.in.ProcessCardPaymentUseCase;
import com.mansa.application.port.in.RefundPaymentUseCase;
import com.mansa.infrastructure.security.filter.CorrelationIdFilter;
import com.mansa.domain.model.CardPayment;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Card Payments", description = "Card payment processing endpoints")
public class CardPaymentController {

    private final ProcessCardPaymentUseCase processUseCase;
    private final CapturePaymentUseCase     captureUseCase;
    private final RefundPaymentUseCase      refundUseCase;
    private final GetTransactionUseCase     getTransactionUseCase;
    private final PaymentApiMapper          mapper;

 

    // ── POST /api/v1/payments ─────────────────────────────────────────────────
    @PostMapping
    @Operation(
        summary  = "Process a card payment",
        description = "Authorizes a card payment via Stripe. Returns the transaction with its status.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Payment processed",
                content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "422", description = "Payment rejected by gateway")
        }
    )
    public ResponseEntity< PaymentResponse/*String*/> processPayment(
             @RequestBody PaymentRequest request,
            HttpServletRequest httpRequest) {
                
          System.out.println("CONTROLLER ENTERED");      
         
        String correlationId = resolveCorrelationId(httpRequest);
        System.out.println("REQUEST = " + request);
        log.info("[{}] POST /api/v1/payments – merchant={}", correlationId, request.getMerchantId());
 // Command simplifié — plus de données brutes de carte
        ProcessCardPaymentUseCase.Command command = new ProcessCardPaymentUseCase.Command(
                request.getPaymentMethodId(),
                request.getAmount(),
                request.getCurrency(),
                request.getMerchantId(),
                request.getDescription(),
                correlationId
        );
        System.out.println("BEFORE processUseCase.process()");
        CardPayment result = processUseCase.process(command);
        System.out.println("AFTER processUseCase.process()");
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(result));
        // return ResponseEntity.ok("HELLO");
    }

    // ── GET /api/v1/payments/{transactionId} ──────────────────────────────────
    @GetMapping("/{transactionId}")
    @Operation(summary = "Get transaction by ID")
    public ResponseEntity<PaymentResponse> getTransaction(
            @Parameter(description = "Transaction ID") @PathVariable String transactionId) {

        CardPayment payment = getTransactionUseCase.getByTransactionId(transactionId);
        return ResponseEntity.ok(mapper.toResponse(payment));
    }

    // ── POST /api/v1/payments/{transactionId}/capture ─────────────────────────
    @PostMapping("/{transactionId}/capture")
    @Operation(summary = "Capture an authorized payment")
    public ResponseEntity<PaymentResponse> capture(@PathVariable String transactionId) {
        CardPayment result = captureUseCase.capture(transactionId);
        return ResponseEntity.ok(mapper.toResponse(result));
    }

    // ── POST /api/v1/payments/{transactionId}/refund ──────────────────────────
    @PostMapping("/{transactionId}/refund")
    @Operation(summary = "Refund a captured payment")
    public ResponseEntity<PaymentResponse> refund(@PathVariable String transactionId) {
        CardPayment result = refundUseCase.refund(transactionId);
        return ResponseEntity.ok(mapper.toResponse(result));
    }

    private String resolveCorrelationId(HttpServletRequest request) {
        String id = request.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
        return id != null ? id : java.util.UUID.randomUUID().toString();
    }
}
 