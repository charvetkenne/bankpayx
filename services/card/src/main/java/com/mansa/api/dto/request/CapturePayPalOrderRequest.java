package com.mansa.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CapturePayPalOrderRequest {

    @NotBlank(message = "orderId requis — reçu depuis returnUrl?token=ORDER_ID")
    private String orderId;
}
