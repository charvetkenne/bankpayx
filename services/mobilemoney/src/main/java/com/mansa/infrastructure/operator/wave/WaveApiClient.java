package com.mansa.infrastructure.operator.wave;


import com.mansa.infrastructure.operator.wave.config.WaveProperties;
import com.mansa.infrastructure.operator.wave.dto.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class WaveApiClient {

    private static final String CIRCUIT_BREAKER_NAME = "wave-operator";
    private static final String RETRY_NAME = "wave-operator";

    private final WebClient waveWebClient;
    private final WaveProperties waveProperties;

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME)
    @Retry(name = RETRY_NAME)
    public WavePaymentRequest checkout(WavePaymentRequest request) {
        log.info("Wave checkout: clientRef={}", request.clientReference());
        try {
            return waveWebClient.post()
                    .uri("/v1/checkout/sessions")
                    .header("Authorization", "Bearer " + waveProperties.apiKey())
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(WavePaymentRequest.class)
                    .block();
        } catch (Exception e) {
            log.error("Wave checkout failed: {}", e.getMessage());
            throw new RuntimeException("Wave API error: " + e.getMessage(), e);
        }
    }

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME)
    @Retry(name = RETRY_NAME)
    public WaveStatusResponse getCheckoutStatus(String checkoutId) {
        log.debug("Wave status check: checkoutId={}", checkoutId);
        try {
            return waveWebClient.get()
                    .uri("/v1/checkout/sessions/{checkoutId}", checkoutId)
                    .header("Authorization", "Bearer " + waveProperties.apiKey())
                    .retrieve()
                    .bodyToMono(WaveStatusResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("Wave status check failed: {}", e.getMessage());
            throw new RuntimeException("Wave status API error: " + e.getMessage(), e);
        }
    }
}
