package com.mansa.api.controller;


import com.mansa.api.response.ApiErrorResponse;
//import com.mansa.domain.exception.*;
import com.mansa.domain.execption.DuplicateTransactionException;
import com.mansa.domain.execption.HmacValidationException;
import com.mansa.domain.execption.InvalidTransactionStateException;
import com.mansa.domain.execption.OperatorUnavailableException;
import com.mansa.domain.execption.TransactionNotFoundException;
import com.mansa.infrastructure.monitoring.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            TransactionNotFoundException ex, HttpServletRequest request) {
        log.warn("Transaction not found: {}", ex.getTransactionId());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of("TRANSACTION_NOT_FOUND", ex.getMessage(),
                        correlationId(request)));
    }

    @ExceptionHandler(DuplicateTransactionException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicate(
            DuplicateTransactionException ex, HttpServletRequest request) {
        log.warn("Duplicate transaction: {}", ex.getIdempotencyKey());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of("DUPLICATE_TRANSACTION", ex.getMessage(),
                        correlationId(request)));
    }

    @ExceptionHandler(InvalidTransactionStateException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidState(
            InvalidTransactionStateException ex, HttpServletRequest request) {
        log.warn("Invalid state transition: {} -> {}", ex.getCurrentStatus(), ex.getTargetStatus());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiErrorResponse.of("INVALID_STATE_TRANSITION", ex.getMessage(),
                        correlationId(request)));
    }

    @ExceptionHandler(HmacValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleHmac(
            HmacValidationException ex, HttpServletRequest request) {
        log.warn("HMAC validation failed: operator={}", ex.getOperatorCode());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiErrorResponse.of("HMAC_VALIDATION_FAILED", "Invalid callback signature",
                        correlationId(request)));
    }

    @ExceptionHandler(OperatorUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleOperatorUnavailable(
            OperatorUnavailableException ex, HttpServletRequest request) {
        log.error("Operator unavailable: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiErrorResponse.of("OPERATOR_UNAVAILABLE", ex.getMessage(),
                        correlationId(request)));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.withDetails("VALIDATION_ERROR",
                        "Request validation failed", errors, correlationId(request)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of("INTERNAL_ERROR",
                        "An unexpected error occurred", correlationId(request)));
    }

    private String correlationId(HttpServletRequest request) {
        return request.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
    }
}
