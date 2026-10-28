package com.mansa.infrastructure.operator.cinetpay;


import com.mansa.infrastructure.operator.cinetpay.config.CinetPayProperties;
import com.mansa.infrastructure.operator.cinetpay.dto.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
 
@Slf4j
@Component
@RequiredArgsConstructor
public class CinetPayApiClient {

    private static final String CIRCUIT_BREAKER_NAME = "cinetpay-operator";
    private static final String RETRY_NAME = "cinetpay-operator";

    private final WebClient cinetPayWebClient;
    private final CinetPayProperties cinetPayProperties;

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME)
    @Retry(name = RETRY_NAME)
    public CinetPayPaymentResponse initiatePayment(CinetPayPaymentRequest request) {
        log.info("CinetPay initiate payment: transactionId={}", request.transactionId());
        try {
            return cinetPayWebClient.post()
                    .uri("/payment")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(CinetPayPaymentResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("CinetPay payment initiation failed: {}", e.getMessage());
            throw new RuntimeException("CinetPay API error: " + e.getMessage(), e);
        }
    }

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME)
    @Retry(name = RETRY_NAME)
    public CinetPayStatusResponse checkPayment(String apiKey, String siteId, String transactionId) {
        log.debug("CinetPay status check: transactionId={}", transactionId);
        try {
            return cinetPayWebClient.post()
                    .uri("/check")
                    .bodyValue(Map.of(
                            "apikey", apiKey,
                            "site_id", siteId,
                            "transaction_id", transactionId
                    ))
                    .retrieve()
                    .bodyToMono(CinetPayStatusResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("CinetPay status check failed: {}", e.getMessage());
            throw new RuntimeException("CinetPay status API error: " + e.getMessage(), e);
        }
    }
}