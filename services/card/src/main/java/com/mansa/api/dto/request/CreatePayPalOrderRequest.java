package com.mansa.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreatePayPalOrderRequest {

    @NotNull @DecimalMin("0.01")
    private Double amount;

    @NotBlank
    @Pattern(regexp = "EUR|USD|GBP|MAD",
             message = "PayPal supporte : EUR, USD, GBP, MAD (pas XOF/XAF)")
    private String currency;

    @NotBlank
    private String merchantId;

    @Size(max = 255)
    private String description;

    @NotBlank(message = "returnUrl requis — PayPal y redirige après approbation")
    private String returnUrl;

    @NotBlank(message = "cancelUrl requis — PayPal y redirige si l'utilisateur annule")
    private String cancelUrl;
}
