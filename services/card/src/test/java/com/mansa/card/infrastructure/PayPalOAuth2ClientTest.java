package com.mansa.card.infrastructure;

import com.mansa.infrastructure.paypal.auth.PayPalOAuth2Client;
import com.mansa.infrastructure.paypal.config.PayPalConfig;
import com.mansa.infrastructure.paypal.dto.PayPalTokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PayPalOAuth2ClientTest {

    @Test
    void tokenResponse_parsesCorrectly() {
        PayPalTokenResponse response = new PayPalTokenResponse();
        response.setAccessToken("A21AAtest");
        response.setExpiresIn(32400L);
        response.setTokenType("Bearer");

        assertThat(response.getAccessToken()).isEqualTo("A21AAtest");
        assertThat(response.getExpiresIn()).isEqualTo(32400L);
    }

    @Test
    void paypalConfig_defaultBaseUrl_isSandbox() {
        PayPalConfig config = new PayPalConfig();
        assertThat(config.getBaseUrl()).isEqualTo("https://api-m.sandbox.paypal.com");
    }
}
