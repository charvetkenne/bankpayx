package com.mansa.infrastructure.operator.orange;


import com.mansa.infrastructure.operator.orange.config.OrangeProperties;
import com.mansa.infrastructure.operator.orange.dto.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrangeApiClient {

    private static final String CIRCUIT_BREAKER_NAME = "orange-operator";
    private static final String RETRY_NAME = "orange-operator";

    private final WebClient orangeWebClient;
    private final OrangeProperties orangeProperties;
    private final ConcurrentHashMap<String, String> tokenCache = new ConcurrentHashMap<>();

    private String getAccessToken() {
        return tokenCache.computeIfAbsent("token", k -> {
            String credentials = Base64.getEncoder().encodeToString(
                    (orangeProperties.clientId() + ":" + orangeProperties.clientSecret())
                            .getBytes(StandardCharsets.UTF_8)
            );
            try {
                Map<?, ?> resp = orangeWebClient.post()
                        .uri("/oauth/token")
                        .header("Authorization", "Basic " + credentials)
                        .bodyValue("grant_type=client_credentials")
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();
                return resp != null ? (String) resp.get("access_token") : "orange-simulated-token";
            } catch (Exception e) {
                log.warn("Could not obtain Orange token, using simulated: {}", e.getMessage());
                return "orange-simulated-token-" + System.currentTimeMillis();
            }
        });
    }

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME)
    @Retry(name = RETRY_NAME)
    public OrangePaymentResponse initiatePayment(OrangePaymentRequest request) {
        log.info("Orange initiatePayment: orderId={}", request.orderId());
        try {
            return orangeWebClient.post()
                    .uri("/webpayment/v1/cashIn")
                    .header("Authorization", "Bearer " + getAccessToken())
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(OrangePaymentResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("Orange payment initiation failed: {}", e.getMessage());
            throw new RuntimeException("Orange API error: " + e.getMessage(), e);
        }
    }

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME)
    @Retry(name = RETRY_NAME)
    public OrangeStatusResponse getPaymentStatus(String transactionId) {
        log.debug("Orange status check: transactionId={}", transactionId);
        try {
            return orangeWebClient.get()
                    .uri("/webpayment/v1/cashIn/{transactionId}", transactionId)
                    .header("Authorization", "Bearer " + getAccessToken())
                    .retrieve()
                    .bodyToMono(OrangeStatusResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("Orange status check failed: {}", e.getMessage());
            throw new RuntimeException("Orange status API error: " + e.getMessage(), e);
        }
    }
}
