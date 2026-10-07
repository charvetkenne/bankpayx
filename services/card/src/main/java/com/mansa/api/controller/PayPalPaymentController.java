package com.mansa.api.controller;

import com.mansa.api.dto.request.CapturePayPalOrderRequest;
import com.mansa.api.dto.request.CreatePayPalOrderRequest;
import com.mansa.api.dto.response.CapturePayPalOrderResponse;
import com.mansa.api.dto.response.CreatePayPalOrderResponse;
import com.mansa.application.port.in.CapturePayPalOrderUseCase;
import com.mansa.application.port.in.CreatePayPalOrderUseCase;
import com.mansa.domain.model.PayPalOrder;
import com.mansa.infrastructure.security.filter.CorrelationIdFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/paypal")
@RequiredArgsConstructor
@Tag(name = "PayPal Payments", description = "Paiements PayPal via API REST officielle PayPal Sandbox")
public class PayPalPaymentController {

    private final CreatePayPalOrderUseCase  createOrderUseCase;
    private final CapturePayPalOrderUseCase captureOrderUseCase;

    // ── POST /api/v1/paypal/create-order ──────────────────────────────────────
    @PostMapping("/create-order")
    @Operation(
        summary     = "Créer un Order PayPal",
        description = "Retourne approveUrl → le frontend redirige l'utilisateur vers PayPal."
    )
    public ResponseEntity<CreatePayPalOrderResponse> createOrder(
            @Valid @RequestBody CreatePayPalOrderRequest request,
            HttpServletRequest httpRequest) {

        String correlationId = resolveCorrelationId(httpRequest);
        log.info("[{}] POST /api/v1/paypal/create-order merchant={} amount={}",
                correlationId, request.getMerchantId(), request.getAmount());

        PayPalOrder order = createOrderUseCase.execute(
                new CreatePayPalOrderUseCase.Command(
                        request.getAmount(), request.getCurrency(),
                        request.getMerchantId(), request.getDescription(),
                        request.getReturnUrl(), request.getCancelUrl(),
                        correlationId
                )
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(
                CreatePayPalOrderResponse.builder()
                        .transactionId(order.getTransactionId())
                        .orderId(order.getPaypalOrderId())
                        .approveUrl(order.getApproveUrl())
                        .status(order.getStatus().name())
                        .message("Redirect user to approveUrl to complete PayPal payment")
                        .build()
        );
    }

    // ── POST /api/v1/paypal/capture-order ─────────────────────────────────────
    @PostMapping("/capture-order")
    @Operation(
        summary     = "Capturer un Order PayPal",
        description = "Appelé par le frontend après retour de PayPal (returnUrl?token=ORDER_ID)."
    )
    public ResponseEntity<CapturePayPalOrderResponse> captureOrder(
            @Valid @RequestBody CapturePayPalOrderRequest request,
            HttpServletRequest httpRequest) {

        String correlationId = resolveCorrelationId(httpRequest);
        log.info("[{}] POST /api/v1/paypal/capture-order orderId={}",
                correlationId, request.getOrderId());

        PayPalOrder order = captureOrderUseCase.execute(
                new CapturePayPalOrderUseCase.Command(request.getOrderId(), correlationId)
        );

        return ResponseEntity.ok(
                CapturePayPalOrderResponse.builder()
                        .transactionId(order.getTransactionId())
                        .orderId(order.getPaypalOrderId())
                        .captureId(order.getCaptureId())
                        .status(order.getStatus().name())
                        .amount(order.getAmount().getAmount())
                        .currency(order.getAmount().getCurrency().name())
                        .message("CAPTURED".equals(order.getStatus().name())
                                ? "Payment captured successfully"
                                : "Capture failed: " + order.getFailureReason())
                        .build()
        );
    }

    private String resolveCorrelationId(HttpServletRequest request) {
        String id = request.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
        return id != null ? id : UUID.randomUUID().toString();
    }
}
