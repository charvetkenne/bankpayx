package com.mansa.api.response;



import com.mansa.domain.valueobject.TransactionStatus;

import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "Résultat de l'initiation de paiement")
public record PaymentInitiationResponse(
        @Schema(description = "UUID de la transaction BankPayX", example = "550e8400-e29b-41d4-a716-446655440000")
        String transactionId,
         @Schema(description = "Statut courant", example = "PROCESSING")
        TransactionStatus status,
        @Schema(description = "Opérateur effectivement utilisé (peut différer si fallback CinetPay)", example = "MTN")
        String operatorCode,
         @Schema(description = "true si la clé d'idempotence existait deja")
        boolean isDuplicate,
        @Schema(description = "Message lisible")
        String message
) {}
