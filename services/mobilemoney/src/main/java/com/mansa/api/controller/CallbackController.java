package com.mansa.api.controller;


import com.mansa.application.port.in.HandleCallbackPort;
import com.mansa.application.usecase.HandleCallbackUseCase;
import com.mansa.infrastructure.monitoring.MobileMoneyMetrics;
import com.mansa.infrastructure.operator.notchpay.dto.NotchPayWebhookPayload;
import com.mansa.infrastructure.security.HmacSignatureValidator;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Tag(name="Operator Callback" , description="Endpoints de notification des operateur - authentifies par HMAC, pas par JWT")
@Slf4j
@RestController
@RequestMapping("/api/v1/callbacks")
@RequiredArgsConstructor
public class CallbackController {

    private final HandleCallbackPort handleCallbackPort;
    private final HmacSignatureValidator hmacValidator;
    private final MobileMoneyMetrics metrics;
    private final ObjectMapper objectMapper;


     @Operation(
        summary = "Callback MTN Mobile Money",
        description = "Reçoit les notifications asynchrones de MTN. Signature HMAC-SHA256 validée via l'en-tête X-Callback-Signature.",
        security = {}   // ← Retire l'exigence JWT pour ce endpoint dans Swagger
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Callback traité"),
        @ApiResponse(responseCode = "401", description = "Signature HMAC invalide")
    })
    @PostMapping("/mtn")
    public ResponseEntity<Void> handleMtnCallback(
            @RequestBody String rawPayload,
            @RequestHeader(value = "X-Callback-Signature", required = false) String signature,
            HttpServletRequest request) throws IOException {

        String operatorCode = "MTN";
        validateAndProcess(rawPayload, signature, operatorCode, "externalId", "status",
                "financialTransactionId", null, null);
        return ResponseEntity.ok().build();
    }
    @Operation(summary = "Callback Ornage Money" , security={})
    @PostMapping("/orange")
    public ResponseEntity<Void> handleOrangeCallback(
            @RequestBody String rawPayload,
            @RequestHeader(value = "X-Orange-Signature", required = false) String signature) throws IOException {

        String operatorCode = "ORANGE";
        validateAndProcess(rawPayload, signature, operatorCode, "order_id", "status",
                "txnid", null, null);
        return ResponseEntity.ok().build();
    }
    
    @Operation(summary = "Callback Wave", security = {})
    @PostMapping("/wave")
    public ResponseEntity<Void> handleWaveCallback(
            @RequestBody String rawPayload,
            @RequestHeader(value = "Wave-Signature", required = false) String signature) throws IOException {

        String operatorCode = "WAVE";
        validateAndProcess(rawPayload, signature, operatorCode, "client_reference",
                "checkout_status", "transaction_id", null, null);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Callback CinetPay", security = {})
    @PostMapping("/cinetpay")
    public ResponseEntity<Void> handleCinetPayCallback(
            @RequestBody String rawPayload,
            @RequestHeader(value = "X-CinetPay-Signature", required = false) String signature) throws IOException {

        String operatorCode = "CINETPAY";
        JsonNode payload = objectMapper.readTree(rawPayload);

        String cpmResult = payload.path("cpm_result").asText("");
        String mappedStatus = "00".equals(cpmResult) ? "SUCCESS" : "FAILED";

        String transactionId = payload.path("cpm_trans_id").asText();
        String operatorId = payload.path("cel_phone_num").asText();
        String errorMessage = payload.path("cpm_error_message").asText();

        if (signature != null) {
            hmacValidator.validate(operatorCode, rawPayload, signature);
        }

        metrics.recordCallbackReceived(operatorCode);

        HandleCallbackUseCase.CallbackCommand command = new HandleCallbackUseCase.CallbackCommand(
                transactionId,
                operatorCode,
                operatorId,
                mappedStatus,
                cpmResult,
                errorMessage,
                rawPayload,
                signature
        );

        handleCallbackPort.handleCallback(command);
        return ResponseEntity.ok().build();
    }

    @Operation(
    summary     = "Webhook NotchPay",
    description = "Reçoit les notifications NotchPay. Signature HMAC-SHA256 validée via 'x-notch-signature'.",
    security    = {}
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Webhook traité avec succès"),
        @ApiResponse(responseCode = "401", description = "Signature HMAC invalide")
    })
    @PostMapping("/notchpay")
    public ResponseEntity<Void> handleNotchPayCallback(
            @RequestBody String rawPayload,
            @RequestHeader(value = "x-notch-signature", required = false) String signature)
            throws IOException {

        String operatorCode = "NOTCHPAY";

        // Validation de la signature HMAC-SHA256
        if (signature != null) {
            hmacValidator.validate(operatorCode, rawPayload, signature);
        } else {
            log.warn("NotchPay webhook received without x-notch-signature header");
        }

        metrics.recordCallbackReceived(operatorCode);

        NotchPayWebhookPayload payload = objectMapper.readValue(rawPayload, NotchPayWebhookPayload.class);

        if (payload.data() == null) {
            log.warn("NotchPay webhook received with null data, event={}, ignoring", payload.event());
            return ResponseEntity.ok().build();
        }

        String domainStatus  = payload.toDomainStatus();
        String failureCode   = payload.toFailureCode();

        log.info("NotchPay webhook: event={}, reference={}, status={}",
                payload.event(), payload.data().reference(), domainStatus);

        HandleCallbackUseCase.CallbackCommand command = new HandleCallbackUseCase.CallbackCommand(
                payload.data().reference(),      // reference NotchPay = notre transactionId BankPayX
                operatorCode,
                payload.data().reference(),      // operator reference confirmée
                domainStatus,
                failureCode,
                payload.event(),
                rawPayload,
                signature
        );

        handleCallbackPort.handleCallback(command);
        return ResponseEntity.ok().build();
    }

    private void validateAndProcess(
            String rawPayload,
            String signature,
            String operatorCode,
            String transactionIdField,
            String statusField,
            String operatorRefField,
            String failureCodeField,
            String failureMessageField) throws IOException {

        if (signature != null) {
            hmacValidator.validate(operatorCode, rawPayload, signature);
        }

        metrics.recordCallbackReceived(operatorCode);

        JsonNode payload = objectMapper.readTree(rawPayload);

        String transactionId = payload.path(transactionIdField).asText();
        String status = payload.path(statusField).asText();
        String operatorRef = payload.path(operatorRefField).asText("");
        String failureCode = failureCodeField != null ? payload.path(failureCodeField).asText("") : null;
        String failureMessage = failureMessageField != null ? payload.path(failureMessageField).asText("") : null;

        HandleCallbackUseCase.CallbackCommand command = new HandleCallbackUseCase.CallbackCommand(
                transactionId,
                operatorCode,
                operatorRef.isEmpty() ? null : operatorRef,
                status,
                failureCode,
                failureMessage,
                rawPayload,
                signature
        );

        handleCallbackPort.handleCallback(command);
    }
}