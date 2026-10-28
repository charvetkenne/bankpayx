package com.mansa.card.infrastructure;

import com.mansa.domain.enums.Currency;
import com.mansa.domain.model.CardPayment;
import com.mansa.domain.model.Money;
import com.mansa.infrastructure.stripe.dto.StripePaymentRequest;
import com.mansa.infrastructure.stripe.mapper.StripeMapper;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class StripeMapperTest {

    private final StripeMapper mapper = new StripeMapper();

    @Test
    void toStripeRequest_mapsAllFieldsCorrectly() {
        CardPayment payment = CardPayment.initiate(
                Money.of(25.50, Currency.EUR),
                "merchant_1", "Test payment", "corr-001", "pm_test_visa"
        );

        StripePaymentRequest request = mapper.toStripeRequest(payment);

        assertThat(request.getAmountInCents()).isEqualTo(2550L);
        assertThat(request.getCurrency()).isEqualTo("eur");
        assertThat(request.getPaymentMethodId()).isEqualTo("pm_test_visa");
        assertThat(request.getMerchantId()).isEqualTo("merchant_1");
        assertThat(request.getCorrelationId()).isEqualTo("corr-001");
    }
}
