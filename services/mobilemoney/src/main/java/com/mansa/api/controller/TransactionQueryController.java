package com.mansa.api.controller;



import com.mansa.api.mapper.MobileMoneyApiMapper;
import com.mansa.api.response.TransactionStatusResponse;
import com.mansa.application.port.in.CheckTransactionStatusPort;
import com.mansa.application.usecase.CheckTransactionStatusUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Transaction Query", description = "Consulter le statut des transactions")
@Slf4j
@RestController
@RequestMapping("/api/v1/mobile-money")
@RequiredArgsConstructor
public class TransactionQueryController {

    private final CheckTransactionStatusPort checkTransactionStatusPort;
    private final MobileMoneyApiMapper mapper;
    @Operation(summary = "Statut d'une transaction", description = "Retourne le statut courant, la référence opérateur et la raison d'échec éventuelle.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Statut retourné"),
        @ApiResponse(responseCode = "404", description = "Transaction introuvable"),
        @ApiResponse(responseCode = "403", description = "Rôle PAYMENT_READ ou ADMIN requis")
    })

    @GetMapping("/payments/{transactionId}")
    @PreAuthorize("hasRole('PAYMENT_READ') or hasRole('ADMIN')")
    public ResponseEntity<TransactionStatusResponse> getTransactionStatus(
            @PathVariable String transactionId) {

        log.debug("Status query: transactionId={}", transactionId);
        CheckTransactionStatusUseCase.TransactionStatusResult result =
                checkTransactionStatusPort.checkStatus(transactionId);
        return ResponseEntity.ok(mapper.toResponse(result));
    }
}