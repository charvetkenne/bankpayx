package com.mansa.api.request;


import jakarta.validation.constraints.*;
import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requête d'initiation de paiement Mobile Money")
public record InitiatePaymentRequest(


        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+?[1-9]\\d{6,14}$", message = "Invalid international phone number format")
        String phoneNumber,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "1.00", message = "Amount must be at least 1.00")
        @Digits(integer = 12, fraction = 4, message = "Invalid amount format")
        BigDecimal amount,

        @NotBlank(message = "Currency code is required")
        @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters (ISO 4217)")
        String currencyCode,

        @NotBlank(message = "Operator code is required")
        @Pattern(
            regexp = "^(mtn|orange|wave|fedapay|notchpay)$",  // ← notchpay ajouté
            flags  = Pattern.Flag.CASE_INSENSITIVE,
            message = "Operator must be one of: mtn, orange, wave, fedapay, notchpay"
        )
        String operatorCode,

        @NotBlank(message = "Customer ID is required")
        @Size(max = 100, message = "Customer ID must not exceed 100 characters")
        String customerId

) {}
