package com.mansa.infrastructure.paypal.auth;

import com.mansa.infrastructure.paypal.config.PayPalConfig;
import com.mansa.infrastructure.paypal.dto.PayPalTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

/**
 * Gère le token OAuth2 PayPal (client_credentials).
 * Le token est mis en cache et renouvelé automatiquement avant expiration.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayPalOAuth2Client {

    private final PayPalConfig paypalConfig;

    private String  cachedToken;
    private Instant tokenExpiry = Instant.MIN;

    /** Retourne un access token valide (depuis le cache ou en le renouvelant). */
    public synchronized String getAccessToken() {
        if (cachedToken == null || Instant.now().isAfter(tokenExpiry.minusSeconds(60))) {
            log.debug("PayPal token expired or absent — fetching new token");
            refreshToken();
        }
        return cachedToken;
    }

    private void refreshToken() {
        String credentials = paypalConfig.getClientId() + ":" + paypalConfig.getClientSecret();
        String basicAuth   = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        RestClient client = RestClient.create();

        PayPalTokenResponse response = client.post()
                .uri(paypalConfig.getBaseUrl() + "/v1/oauth2/token")
                .header("Authorization", "Basic " + basicAuth)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(PayPalTokenResponse.class);

        if (response == null || response.getAccessToken() == null) {
            throw new IllegalStateException("Failed to obtain PayPal access token");
        }

        this.cachedToken  = response.getAccessToken();
        this.tokenExpiry  = Instant.now().plusSeconds(response.getExpiresIn());

        log.info("PayPal OAuth2 token refreshed, expires in {}s", response.getExpiresIn());
    }
}
