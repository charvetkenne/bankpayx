package com.mansa.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter @Builder
public class CapturePayPalOrderResponse {
    private String     transactionId;
    private String     orderId;
    private String     captureId;
    private String     status;        // CAPTURED ou FAILED
    private BigDecimal amount;
    private String     currency;
    private String     message;
}
