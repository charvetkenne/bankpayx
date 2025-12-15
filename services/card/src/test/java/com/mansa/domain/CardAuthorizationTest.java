package com.mansa.domain;

import com.mansa.domain.aggregate.CardAuthorization;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CardAuthorizationTest {

    @Test
    void authorize_valid_small_amount_should_authorize() {
        CardAuthorization auth = new CardAuthorization("4111111111111111", new BigDecimal("5.00"), "EUR", "MID-1", "RRN-1");
        CardAuthorization.Decision d = auth.authorize("2512");
        assertThat(d.authorized).isTrue();
        assertThat(auth.getTransaction().getStatus()).isEqualTo("AUTHORIZED");
        assertThat(auth.getTransaction().getAuthCode()).isNotNull();
    }

    @Test
    void authorize_too_large_amount_should_decline() {
        CardAuthorization auth = new CardAuthorization("4111111111111111", new BigDecimal("200000"), "EUR", "MID-1", "RRN-2");
        CardAuthorization.Decision d = auth.authorize("2512");
        assertThat(d.authorized).isFalse();
    }
}
