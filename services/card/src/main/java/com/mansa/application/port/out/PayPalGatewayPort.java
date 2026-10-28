package com.mansa.application.port.out;

public interface PayPalGatewayPort {

    /** Crée un Order PayPal et retourne (orderId, approveUrl). */
    OrderResult createOrder(long amountInCents, String currency,
                            String description, String returnUrl, String cancelUrl,
                            String correlationId);

    /** Capture un Order PayPal approuvé. Retourne le captureId. */
    String captureOrder(String paypalOrderId);

    /** Récupère le statut actuel d'un Order PayPal. */
    String getOrderStatus(String paypalOrderId);

    record OrderResult(String orderId, String approveUrl) {}
}
