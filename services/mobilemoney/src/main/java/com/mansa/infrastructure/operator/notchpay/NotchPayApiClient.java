package com.mansa.infrastructure.operator.notchpay;


import com.mansa.infrastructure.operator.notchpay.config.NotchPayProperties;
import com.mansa.infrastructure.operator.notchpay.dto.NotchPayInitRequest;
import com.mansa.infrastructure.operator.notchpay.dto.NotchPayInitResponse;
import com.mansa.infrastructure.operator.notchpay.dto.NotchPayVerifyResponse;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotchPayApiClient {

    private static final String CIRCUIT_BREAKER_NAME = "notchpay-operator";
    private static final String RETRY_NAME           = "notchpay-operator";

    private final WebClient notchPayWebClient;
    private final NotchPayProperties notchPayProperties;

    /**
     * Initialise un paiement NotchPay.
     *
     * POST https://api.notchpay.co/payments/initialize
     * Header : Authorization: {publicKey}
     *
     * Retourne un authorization_url vers lequel rediriger le client,
     * et une reference unique de transaction pour le suivi.
     */
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME)
    @Retry(name = RETRY_NAME)
    public NotchPayInitResponse initializePayment(NotchPayInitRequest request) {
        log.info("NotchPay initializePayment: reference={}, amount={} {}",
                request.reference(), request.amount(), request.currency());

        return notchPayWebClient.post()
                .uri("/payments/initialize")
                .header("Authorization", notchPayProperties.publicKey())
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, resp ->
                        resp.bodyToMono(String.class).flatMap(body -> {
                            log.error("NotchPay 4xx on initializePayment: {}", body);
                            return Mono.error(new RuntimeException("NotchPay client error: " + body));
                        }))
                .onStatus(HttpStatusCode::is5xxServerError, resp ->
                        resp.bodyToMono(String.class).flatMap(body -> {
                            log.error("NotchPay 5xx on initializePayment: {}", body);
                            return Mono.error(new RuntimeException("NotchPay server error: " + body));
                        }))
                .bodyToMono(NotchPayInitResponse.class)
                .block();
    }

    /**
     * Vérifie le statut d'un paiement NotchPay par sa référence.
     *
     * GET https://api.notchpay.co/payments/{reference}
     * Header : Authorization: {publicKey}
     */
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME)
    @Retry(name = RETRY_NAME)
    public NotchPayVerifyResponse verifyPayment(String reference) {
        log.debug("NotchPay verifyPayment: reference={}", reference);

        return notchPayWebClient.get()
                .uri("/payments/{reference}", reference)
                .header("Authorization", notchPayProperties.publicKey())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, resp ->
                        resp.bodyToMono(String.class).flatMap(body -> {
                            log.error("NotchPay 4xx on verifyPayment: reference={}, body={}", reference, body);
                            return Mono.error(new RuntimeException("NotchPay verify error: " + body));
                        }))
                .onStatus(HttpStatusCode::is5xxServerError, resp ->
                        resp.bodyToMono(String.class).flatMap(body ->
                                Mono.error(new RuntimeException("NotchPay server error on verify: " + body))))
                .bodyToMono(NotchPayVerifyResponse.class)
                .block();
    }

    /**
     * Annule un paiement NotchPay.
     *
     * DELETE https://api.notchpay.co/payments/{reference}
     * Headers : Authorization: {publicKey}  |  X-Grant: {privateKey}
     *
     * L'annulation nécessite la clé privée (X-Grant) en plus de la clé publique.
     */
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME)
    @Retry(name = RETRY_NAME)
    public boolean cancelPayment(String reference) {
        log.info("NotchPay cancelPayment: reference={}", reference);

        try {
            notchPayWebClient.delete()
                    .uri("/payments/{reference}", reference)
                    .header("Authorization", notchPayProperties.publicKey())
                    .header("X-Grant", notchPayProperties.privateKey())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp ->
                            resp.bodyToMono(String.class).flatMap(body ->
                                    Mono.error(new RuntimeException("NotchPay cancel error: " + body))))
                    .toBodilessEntity()
                    .block();

            log.info("NotchPay payment cancelled successfully: reference={}", reference);
            return true;

        } catch (Exception e) {
            log.error("NotchPay cancelPayment failed: reference={}, error={}", reference, e.getMessage());
            return false;
        }
    }
}