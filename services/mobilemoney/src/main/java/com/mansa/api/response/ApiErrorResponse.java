package com.mansa.api.response;


import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Réponse d'erreur standard de l'API")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        @Schema(description = "Code d'erreur métier", example = "TRANSACTION_NOT_FOUND")
        String errorCode,
        @Schema(description = "Message explicatif", example = "Transaction not found: 550e8400...")
        String message,
        @Schema(description = "Détails de validation si applicable")
        List<String> details,
        @Schema(description = "ID de corrélation pour le traçage", example = "corr-uuid-001")
        String correlationId,
         @Schema(description = "Timestamp de l'erreur")
        Instant timestamp
) {
    public static ApiErrorResponse of(String errorCode, String message, String correlationId) {
        return new ApiErrorResponse(errorCode, message, null, correlationId, Instant.now());
    }

    public static ApiErrorResponse withDetails(String errorCode, String message,
                                               List<String> details, String correlationId) {
        return new ApiErrorResponse(errorCode, message, details, correlationId, Instant.now());
    }
}
