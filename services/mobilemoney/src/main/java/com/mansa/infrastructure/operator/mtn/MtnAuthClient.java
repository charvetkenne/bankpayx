package com.mansa.infrastructure.operator.mtn;



import com.mansa.infrastructure.operator.mtn.config.MtnProperties;
import com.mansa.infrastructure.operator.mtn.dto.MtnTokenResponse;
import com.mansa.infrastructure.operator.mtn.exception.MtnAuthenticationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class MtnAuthClient {

     private static final String TOKEN_CACHE_KEY = "mtn-access-token";
    private static final int EXPIRY_SAFETY_MARGIN_SECONDS = 60;

    private final MtnTokenExchangeClient mtnTokenExchangeClient;
    private final MtnProperties mtnProperties;
    private final Environment environment;

    private final ConcurrentHashMap<String, CachedToken> tokenCache = new ConcurrentHashMap<>();
    private final ReentrantLock refreshLock = new ReentrantLock();

    public String getAccessToken() {
        CachedToken cached = tokenCache.get(TOKEN_CACHE_KEY);
        if (cached != null && !cached.isExpired()) {
            return cached.token();
        }

        refreshLock.lock();
        try {
            // Double vérification : un autre thread a pu rafraîchir le token
            // pendant qu'on attendait le verrou — évite les appels HTTP en rafale.
            cached = tokenCache.get(TOKEN_CACHE_KEY);
            if (cached != null && !cached.isExpired()) {
                return cached.token();
            }
            return refreshAccessToken();
        } finally {
            refreshLock.unlock();
        }
    }

    private String refreshAccessToken() {
        try {
            MtnTokenResponse response = mtnTokenExchangeClient.fetchToken();
            return cacheAndReturn(response);
        } catch (Exception e) {
            MtnAuthenticationException authException = toAuthenticationException(e);
            log.error("Authentification MTN échouée: {}", authException.getMessage());
            return resolveStubOrPropagate(authException);
        }
    }

    private String cacheAndReturn(MtnTokenResponse response) {
        int expiresIn = response.expiresIn() != null ? response.expiresIn() : 3600;
        Instant expiresAt = Instant.now().plusSeconds(
                Math.max(expiresIn - EXPIRY_SAFETY_MARGIN_SECONDS, 0));
        CachedToken token = new CachedToken(response.accessToken(), expiresAt);
        tokenCache.put(TOKEN_CACHE_KEY, token);
        return token.token();
    }

    private MtnAuthenticationException toAuthenticationException(Exception e) {
        if (e instanceof MtnAuthenticationException already) {
            return already;
        }
        if (e instanceof WebClientResponseException.Unauthorized
                || e instanceof WebClientResponseException.Forbidden
                || e instanceof WebClientResponseException.BadRequest) {
            WebClientResponseException webEx = (WebClientResponseException) e;
            return new MtnAuthenticationException(
                    "Identifiants MTN rejetés (api-user/api-key/subscription-key), statut "
                            + webEx.getStatusCode(), e);
        }
        return new MtnAuthenticationException(
                "Erreur inattendue lors de l'authentification MTN: " + e.getMessage(), e);
    }

    private String resolveStubOrPropagate(MtnAuthenticationException original) {
        boolean stubAllowed = environment.acceptsProfiles(Profiles.of("local | test"))
                && mtnProperties.subTokenEnabled();

        if (!stubAllowed) {
            throw original;
        }

        log.warn("Token MTN SIMULÉ utilisé (profils actifs={}, "
                        + "operators.mtn.stub-token-enabled=true). "
                        + "Ne jamais activer ce flag en production.",
                String.join(",", environment.getActiveProfiles()));

        String stubToken = "mtn-simulated-token-" + UUID.randomUUID();
        tokenCache.put(TOKEN_CACHE_KEY, new CachedToken(stubToken, Instant.now().plusSeconds(300)));
        return stubToken;
    }

    private record CachedToken(String token, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    // private final WebClient mtnWebClient;
    // private final MtnProperties mtnProperties;

    // private final ConcurrentHashMap<String, CachedToken> tokenCache = new ConcurrentHashMap<>();

    // public String getAccessToken() {
    //     CachedToken cached = tokenCache.get("access_token");
    //     if (cached != null && !cached.isExpired()) {
    //         return cached.token();
    //     }

    //     String credentials = Base64.getEncoder().encodeToString(
    //             (mtnProperties.apiKey() + ":" + mtnProperties.apiSecret())
    //                     .getBytes(StandardCharsets.UTF_8)
    //     );

    //     try {
    //         Map<?, ?> response = mtnWebClient.post()
    //                 .uri("/collection/token/")
    //                 .header("Authorization", "Basic " + credentials)
    //                 .header("Ocp-Apim-Subscription-Key", mtnProperties.subscriptionKey())
    //                 .retrieve()
    //                 .bodyToMono(Map.class)
    //                 .block();

    //         if (response != null && response.containsKey("access_token")) {
    //             String token = (String) response.get("access_token");
    //             int expiresIn = response.containsKey("expires_in")
    //                     ? Integer.parseInt(response.get("expires_in").toString()) : 3600;
    //             tokenCache.put("access_token", new CachedToken(token,
    //                     Instant.now().plusSeconds(expiresIn - 60)));
    //             return token;
    //         }
    //     } catch (Exception e) {
    //         log.error("Failed to obtain MTN access token: {}", e.getMessage());
    //     }

    //     // Fallback: return a simulated token in non-production environments
    //     return "mtn-simulated-token-" + System.currentTimeMillis();
    // }

    // private record CachedToken(String token, Instant expiresAt) {
    //     boolean isExpired() {
    //         return Instant.now().isAfter(expiresAt);
    //     }
    // }
}