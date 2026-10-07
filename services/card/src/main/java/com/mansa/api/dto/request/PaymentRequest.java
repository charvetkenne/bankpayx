package com.mansa.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;



@Getter
@Setter
public class PaymentRequest {

    /**
     * Token généré par Stripe.js côté frontend (pm_xxx).
     * Remplace toutes les données brutes de carte (PAN, CVV, expiry…).
     * Le frontend appelle stripe.createPaymentMethod() et envoie l'ID ici.
     */
    @NotBlank(message = "paymentMethodId is required — generate it with Stripe.js")
    private String paymentMethodId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be greater than 0")
    private Double amount;

    @NotBlank(message = "currency is required")
    @Pattern(regexp = "EUR|USD|GBP|XOF|XAF|MAD", message = "Unsupported currency")
    private String currency;

    @NotBlank(message = "merchantId is required")
    private String merchantId;

    @Size(max = 255)
    private String description;
}