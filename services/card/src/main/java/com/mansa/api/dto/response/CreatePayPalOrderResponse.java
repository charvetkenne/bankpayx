package com.mansa.api.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class CreatePayPalOrderResponse {
    private String transactionId;   // ID interne
    private String orderId;         // ID PayPal (ORDER-xxx)
    private String approveUrl;      // URL vers laquelle rediriger le frontend
    private String status;          // PENDING_APPROVAL
    private String message;
}
