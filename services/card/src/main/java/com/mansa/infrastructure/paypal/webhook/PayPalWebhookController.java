package com.mansa.infrastructure.paypal.webhook;

import com.mansa.application.port.in.CapturePayPalOrderUseCase;
import com.mansa.infrastructure.paypal.dto.PayPalWebhookEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Reçoit les webhooks PayPal.
 *
 * Événements principaux :
 *   CHECKOUT.ORDER.APPROVED      → order approuvé, en attente de capture
 *   PAYMENT.CAPTURE.COMPLETED    → capture confirmée par PayPal
 *   PAYMENT.CAPTURE.DENIED       → capture refusée
 *
 * En production : vérifier la signature via PayPal Notifications API.
 * https://developer.paypal.com/api/rest/webhooks/
 */
@Slf4j
@RestController
@RequestMapping("/webhooks/paypal")
@RequiredArgsConstructor
public class PayPalWebhookController {

    private final CapturePayPalOrderUseCase captureUseCase;
    private final ObjectMapper              objectMapper;

    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "PayPal-Transmission-Id",  required = false) String transmissionId,
            @RequestHeader(value = "PayPal-Transmission-Sig", required = false) String signature) {

        log.info("PayPal webhook received: transmissionId={}", transmissionId);

        try {
            PayPalWebhookEvent event = objectMapper.readValue(payload, PayPalWebhookEvent.class);
            log.info("PayPal webhook eventType={} id={}", event.getEventType(), event.getId());

            switch (event.getEventType() != null ? event.getEventType() : "") {

                case "CHECKOUT.ORDER.APPROVED" -> {
                    String orderId = extractOrderId(event);
                    if (orderId != null) {
                        log.info("PayPal order approved via webhook: orderId={}", orderId);
                        // Capture automatique si la configuration le prévoit
                        // captureUseCase.execute(new CapturePayPalOrderUseCase.Command(orderId, transmissionId));
                    }
                }

                case "PAYMENT.CAPTURE.COMPLETED" -> {
                    log.info("PayPal capture completed via webhook: eventId={}", event.getId());
                    // La capture a déjà été faite par le frontend dans le flow normal.
                    // Ce webhook est une confirmation supplémentaire.
                }

                case "PAYMENT.CAPTURE.DENIED" -> {
                    log.warn("PayPal capture DENIED via webhook: eventId={}", event.getId());
                }

                default -> log.debug("PayPal webhook event ignored: type={}", event.getEventType());
            }

        } catch (Exception ex) {
            log.error("Error processing PayPal webhook: {}", ex.getMessage(), ex);
            return ResponseEntity.internalServerError().body("Webhook processing failed");
        }

        return ResponseEntity.ok("OK");
    }

    @SuppressWarnings("unchecked")
    private String extractOrderId(PayPalWebhookEvent event) {
        try {
            if (event.getResource() instanceof Map<?, ?> resource) {
                return (String) ((Map<String, Object>) resource).get("id");
            }
        } catch (Exception ex) {
            log.warn("Could not extract orderId from webhook resource");
        }
        return null;
    }
}
