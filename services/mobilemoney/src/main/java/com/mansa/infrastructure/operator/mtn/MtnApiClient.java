package com.mansa.infrastructure.operator.mtn;


import com.mansa.infrastructure.operator.mtn.config.MtnProperties;
import com.mansa.infrastructure.operator.mtn.dto.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MtnApiClient {

    private static final String CIRCUIT_BREAKER_NAME = "mtn-operator";
    private static final String RETRY_NAME = "mtn-operator";

    private final WebClient mtnWebClient;
    private final MtnAuthClient mtnAuthClient;
    private final MtnProperties mtnProperties;

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME)
    @Retry(name = RETRY_NAME)
    public MtnPaymentResponse requestToPay(MtnPaymentRequest request, String referenceId) {
        String token = mtnAuthClient.getAccessToken();

        log.info("MTN requestToPay: referenceId={}, externalId={}", referenceId, request.externalId());

        try {
            mtnWebClient.post()
                    .uri("/collection/v1_0/requesttopay")
                    .header("Authorization", "Bearer " + token)
                    .header("X-Reference-Id", referenceId)
                    .header("X-Target-Environment", mtnProperties.environment())
                    .header("Ocp-Apim-Subscription-Key", mtnProperties.subscriptionKey())
                    .header("X-Callback-Url", mtnProperties.callbackUrl())
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, resp ->
                            resp.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("MTN 4xx: " + body))))
                    .onStatus(HttpStatusCode::is5xxServerError, resp ->
                            resp.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("MTN 5xx: " + body))))
                    .toBodilessEntity()
                    .block();

            // MTN returns 202 Accepted — the payment reference is the referenceId we sent
            return new MtnPaymentResponse(referenceId, "PENDING", null);

        } catch (Exception e) {
            log.error("MTN requestToPay failed: referenceId={}, error={}", referenceId, e.getMessage());
            throw new RuntimeException("MTN payment initiation failed: " + e.getMessage(), e);
        }
    }

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME)
    @Retry(name = RETRY_NAME)
    public MtnStatusResponse getRequestToPayStatus(String referenceId) {
        String token = mtnAuthClient.getAccessToken();

        log.debug("MTN status check: referenceId={}", referenceId);

        return mtnWebClient.get()
                .uri("/collection/v1_0/requesttopay/{referenceId}", referenceId)
                .header("Authorization", "Bearer " + token)
                .header("X-Target-Environment", mtnProperties.environment())
                .header("Ocp-Apim-Subscription-Key", mtnProperties.subscriptionKey())
                .retrieve()
                .bodyToMono(MtnStatusResponse.class)
                .block();
    }
}