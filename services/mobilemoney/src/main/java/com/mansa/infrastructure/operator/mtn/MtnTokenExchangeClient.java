package com.mansa.infrastructure.operator.mtn;

import com.mansa.infrastructure.operator.mtn.config.MtnProperties;
import com.mansa.infrastructure.operator.mtn.dto.MtnTokenResponse;
import com.mansa.infrastructure.operator.mtn.exception.MtnAuthenticationException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Client bas niveau responsable UNIQUEMENT de l'échange HTTP avec l'endpoint
 * OAuth2 de MTN MoMo. Séparé de MtnAuthClient afin que les annotations
 * @Retry / @CircuitBreaker soient interceptées par le proxy Spring AOP
 * (un appel interne depuis MtnAuthClient.fetchToken() ne le serait pas).
 *
 * Les erreurs 401/403/400 (identifiants invalides ou requête malformée) sont
 * volontairement exclues du retry et du circuit breaker via la configuration
 * resilience4j.*.ignore-exceptions : retenter avec les mêmes mauvais
 * identifiants ne sert à rien et pollue les métriques du circuit breaker.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MtnTokenExchangeClient {

    private final WebClient mtnWebClient;
    private final MtnProperties mtnProperties;

    @Retry(name = "mtn-auth")
    @CircuitBreaker(name = "mtn-auth", fallbackMethod = "fetchTokenFallback")
    public MtnTokenResponse fetchToken() {
        String credentials = Base64.getEncoder().encodeToString(
                (mtnProperties.apiKey() + ":" + mtnProperties.apiSecret())
                        .getBytes(StandardCharsets.UTF_8)
        );

        // MtnTokenResponse response = mtnWebClient.post()
        //         .uri("/collection/token/")
        //         .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
        //         .header("Ocp-Apim-Subscription-Key", mtnProperties.subscriptionKey())
        //         .retrieve()
        //         .bodyToMono(MtnTokenResponse.class)
        //         .block();

        MtnTokenResponse response = mtnWebClient.post()
                .uri("/collection/token/")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header("Ocp-Apim-Subscription-Key", mtnProperties.subscriptionKey())
                .retrieve()
                .onStatus(status -> true, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(rawBody -> {
                                    if (clientResponse.statusCode().is2xxSuccessful()
                                            && !rawBody.trim().startsWith("{")) {
                                        log.error("MTN a répondu 2xx avec un corps non-JSON: {}", rawBody);
                                        return Mono.error(new MtnAuthenticationException(
                                                "Réponse MTN inattendue (non-JSON): " + clientResponse.statusCode()));
                                    }
                                    return Mono.empty();
                                }))
                .bodyToMono(MtnTokenResponse.class)
                .block();

        if (response == null || response.accessToken() == null || response.accessToken().isBlank()) {
            throw new MtnAuthenticationException(
                    "MTN a répondu sans access_token exploitable");
        }
        return response;
    }

    /**
     * Invoquée par le circuit breaker après épuisement des tentatives de retry,
     * ou immédiatement si le circuit est ouvert (CallNotPermittedException).
     * N'est PAS invoquée pour les exceptions listées en ignore-exceptions
     * (401/403/400), qui remontent brutes jusqu'à MtnAuthClient.
     */
    @SuppressWarnings("unused")
    private MtnTokenResponse fetchTokenFallback(Throwable throwable) {
        log.error("Authentification MTN indisponible après retries/circuit-breaker: {}",
                throwable.getMessage());
        throw new MtnAuthenticationException(
                "Service d'authentification MTN indisponible après plusieurs tentatives",
                throwable);
    }
}
