package com.mansa.infrastructure.paypal.client;

import com.mansa.infrastructure.exception.PayPalException;
import com.mansa.infrastructure.paypal.auth.PayPalOAuth2Client;
import com.mansa.infrastructure.paypal.config.PayPalConfig;
import com.mansa.infrastructure.paypal.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.util.List;

/**
 * Client HTTP bas niveau — appels directs à l'API REST PayPal v2.
 * Toute la logique OAuth2 est déléguée à PayPalOAuth2Client.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayPalHttpClient {

    private final PayPalConfig       paypalConfig;
    private final PayPalOAuth2Client oAuth2Client;

    // ── Create Order ──────────────────────────────────────────────────────────

    /**
     * POST /v2/checkout/orders
     * Crée un Order PayPal avec intent=CAPTURE.
     */
    public PayPalOrderResponse createOrder(long amountInCents, String currency,
                                           String description, String returnUrl,
                                           String cancelUrl, String correlationId) {
        // Convertir les centimes en valeur décimale (ex: 2500 → "25.00")
        String value = BigDecimal.valueOf(amountInCents, 2).toPlainString();

        PayPalCreateOrderRequest request = PayPalCreateOrderRequest.builder()
                .intent("CAPTURE")
                .purchaseUnits(List.of(
                        PayPalCreateOrderRequest.PurchaseUnit.builder()
                                .amount(PayPalCreateOrderRequest.Amount.builder()
                                        .currencyCode(currency.toUpperCase())
                                        .value(value)
                                        .build())
                                .description(description)
                                .build()
                ))
                .applicationContext(PayPalCreateOrderRequest.ApplicationContext.builder()
                        .returnUrl(returnUrl)
                        .cancelUrl(cancelUrl)
                        .brandName(paypalConfig.getBrandName())
                        .userAction("PAY_NOW")
                        .landingPage("LOGIN")
                        .build())
                .build();

        log.debug("PayPal createOrder: amount={} {} correlationId={}", value, currency, correlationId);

        try {
            return restClient()
                    .post()
                    .uri(paypalConfig.getBaseUrl() + "/v2/checkout/orders")
                    .header("PayPal-Request-Id", correlationId) // idempotency key
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(PayPalOrderResponse.class);

        } catch (RestClientResponseException ex) {
            log.error("PayPal createOrder error: status={} body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new PayPalException("PayPal order creation failed: " + ex.getMessage(), ex);
        }
    }

    // ── Capture Order ─────────────────────────────────────────────────────────

    /**
     * POST /v2/checkout/orders/{orderId}/capture
     * Capture un Order approuvé par l'utilisateur.
     */
    public PayPalCaptureResponse captureOrder(String paypalOrderId) {
        log.debug("PayPal captureOrder: orderId={}", paypalOrderId);

        try {
            return restClient()
                    .post()
                    .uri(paypalConfig.getBaseUrl() + "/v2/checkout/orders/{orderId}/capture", paypalOrderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{}")  // corps vide requis par PayPal
                    .retrieve()
                    .body(PayPalCaptureResponse.class);

        } catch (RestClientResponseException ex) {
            log.error("PayPal captureOrder error: orderId={} status={} body={}",
                    paypalOrderId, ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new PayPalException("PayPal capture failed: " + ex.getMessage(), ex);
        }
    }

    // ── Get Order Status ──────────────────────────────────────────────────────

    /**
     * GET /v2/checkout/orders/{orderId}
     */
    public PayPalOrderResponse getOrder(String paypalOrderId) {
        log.debug("PayPal getOrder: orderId={}", paypalOrderId);

        try {
            return restClient()
                    .get()
                    .uri(paypalConfig.getBaseUrl() + "/v2/checkout/orders/{orderId}", paypalOrderId)
                    .retrieve()
                    .body(PayPalOrderResponse.class);

        } catch (RestClientResponseException ex) {
            throw new PayPalException("PayPal getOrder failed: " + ex.getMessage(), ex);
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    /** Construit un RestClient avec le Bearer token PayPal à jour. */
    private RestClient restClient() {
        return RestClient.builder()
                .defaultHeader("Authorization", "Bearer " + oAuth2Client.getAccessToken())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
