package com.mansa.infrastructure.paypal.adapter;

import com.mansa.application.port.out.PayPalGatewayPort;
import com.mansa.infrastructure.paypal.client.PayPalHttpClient;
import com.mansa.infrastructure.paypal.dto.PayPalCaptureResponse;
import com.mansa.infrastructure.paypal.dto.PayPalOrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Implémente PayPalGatewayPort.
 * Traduit les appels domaine en appels PayPalHttpClient.
 * Séparé du client HTTP → testable indépendamment.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayPalGatewayAdapter implements PayPalGatewayPort {

    private final PayPalHttpClient httpClient;

    @Override
    public OrderResult createOrder(long amountInCents, String currency,
                                   String description, String returnUrl, String cancelUrl,
                                   String correlationId) {

        PayPalOrderResponse response = httpClient.createOrder(
                amountInCents, currency, description, returnUrl, cancelUrl, correlationId);

        String orderId    = response.getId();
        String approveUrl = response.getApproveUrl();

        if (approveUrl == null)
            throw new IllegalStateException("PayPal did not return an approve URL for orderId=" + orderId);

        log.info("PayPal order created: orderId={} approveUrl={}", orderId, approveUrl);
        return new OrderResult(orderId, approveUrl);
    }

    @Override
    public String captureOrder(String paypalOrderId) {
        PayPalCaptureResponse response = httpClient.captureOrder(paypalOrderId);

        String captureId = response.extractCaptureId();
        if (captureId == null)
            throw new IllegalStateException("PayPal did not return a captureId for orderId=" + paypalOrderId);

        log.info("PayPal order captured: orderId={} captureId={} status={}",
                paypalOrderId, captureId, response.getStatus());
        return captureId;
    }

    @Override
    public String getOrderStatus(String paypalOrderId) {
        PayPalOrderResponse response = httpClient.getOrder(paypalOrderId);
        return response != null ? response.getStatus() : "UNKNOWN";
    }
}
