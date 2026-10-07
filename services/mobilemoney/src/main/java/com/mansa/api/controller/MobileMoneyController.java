package com.mansa.api.controller;



import com.mansa.api.mapper.MobileMoneyApiMapper;
import com.mansa.api.request.*;
import com.mansa.api.response.*;
import com.mansa.application.port.in.*;
import com.mansa.application.usecase.*;
import com.mansa.infrastructure.monitoring.CorrelationIdFilter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;


@Tag(name ="Mobile Money Payments", description = "Initier, annuler et réessayer des paiements Mobile Money")
@Slf4j
@RestController
@RequestMapping("/api/v1/mobile-money")
@RequiredArgsConstructor
public class MobileMoneyController {

    private final InitiatePaymentPort initiatePaymentPort;
    private final RetryTransactionPort retryTransactionPort;
    private final CancelTransactionPort cancelTransactionPort;
    private final MobileMoneyApiMapper mapper;

    @Operation(
        summary = "Initier un paiement Mobile Money",
        description = "Soumet un paiement vers MTN, Orange ou Wave. En cas d'indisponibilité, bascule automatiquement sur CinetPay."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "202", description = "Paiement initié avec succès",
            content = @Content(schema = @Schema(implementation = PaymentInitiationResponse.class))),
        @ApiResponse(responseCode = "200", description = "Requête dupliquée — transaction existante retournée",
            content = @Content(schema = @Schema(implementation = PaymentInitiationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Données de requête invalides",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Token JWT absent ou invalide"),
        @ApiResponse(responseCode = "503", description = "Tous les opérateurs sont indisponibles")
    })
    @PostMapping("/payments")
    public ResponseEntity<PaymentInitiationResponse> initiatePayment(
            @Valid @RequestBody InitiatePaymentRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpRequest) {

        String correlationId = httpRequest.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);

        InitiatePaymentUseCase.InitiatePaymentCommand command = new InitiatePaymentUseCase.InitiatePaymentCommand(
                request.phoneNumber(),
                request.amount(),
                request.currencyCode(),
                request.operatorCode(),
                request.customerId(),
                idempotencyKey,
                correlationId
        );

        InitiatePaymentUseCase.InitiatePaymentResult result = initiatePaymentPort.initiatePayment(command);
        PaymentInitiationResponse response = mapper.toResponse(result);

        HttpStatus status = result.isDuplicate() ? HttpStatus.OK : HttpStatus.ACCEPTED;
        return ResponseEntity.status(status).body(response);
    }

     @Operation(summary = "Réessayer une transaction échouée", description = "Maximum 3 tentatives. Bascule automatiquement sur CinetPay.")
    @ApiResponses({
        @ApiResponse(responseCode = "202", description = "Nouvelle tentative lancée"),
        @ApiResponse(responseCode = "404", description = "Transaction introuvable"),
        @ApiResponse(responseCode = "422", description = "État invalide pour une nouvelle tentative")
    })
    @PostMapping("/payments/{transactionId}/retry")
    public ResponseEntity<Void> retryTransaction(
            @PathVariable String transactionId,
            @AuthenticationPrincipal Jwt jwt) {

        retryTransactionPort.retryTransaction(transactionId);
        return ResponseEntity.accepted().build();
    }

     @Operation(summary = "Annuler une transaction", description = "Annulable uniquement si statut INITIATED, PENDING ou FAILED.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Transaction annulée"),
        @ApiResponse(responseCode = "422", description = "Transition d'état invalide")
    })
    @PostMapping("/payments/{transactionId}/cancel")
    public ResponseEntity<Void> cancelTransaction(
            @PathVariable String transactionId,
            @Valid @RequestBody CancelTransactionRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        String requestedBy = jwt.getSubject();
        cancelTransactionPort.cancelTransaction(
                new CancelTransactionUseCase.CancelCommand(
                        transactionId,
                        request.cancellationReason(),
                        requestedBy
                )
        );
        return ResponseEntity.noContent().build();
    }
}
