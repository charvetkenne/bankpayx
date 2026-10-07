package com.mansa.card.integration;

import com.mansa.domain.enums.Currency;
import com.mansa.domain.enums.PayPalOrderStatus;
import com.mansa.domain.model.Money;
import com.mansa.domain.model.PayPalOrder;
import com.mansa.infrastructure.paypal.persistence.adapter.PayPalOrderPersistenceAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PayPalOrderPersistenceAdapter.class,
         com.mansa.infrastructure.paypal.persistence.mapper.PayPalOrderMapper.class})
class PayPalOrderPersistenceIT {

    @Autowired PayPalOrderPersistenceAdapter adapter;

    private PayPalOrder buildOrder(String merchantId) {
        PayPalOrder order = PayPalOrder.initiate(
                Money.of(25.00, Currency.EUR),
                merchantId, "Commande test",
                "http://return", "http://cancel", "corr-001"
        );
        order.markCreated("ORDER-123-" + System.nanoTime(),
                "https://sandbox.paypal.com/checkoutnow?token=ORDER-123");
        order.pullDomainEvents();
        return order;
    }

    @Test
    void save_andFindByPaypalOrderId_returnsCorrectOrder() {
        PayPalOrder saved = adapter.save(buildOrder("merchant_1"));

        Optional<PayPalOrder> found = adapter.findByPaypalOrderId(saved.getPaypalOrderId());

        assertThat(found).isPresent();
        assertThat(found.get().getMerchantId()).isEqualTo("merchant_1");
        assertThat(found.get().getStatus()).isEqualTo(PayPalOrderStatus.PENDING_APPROVAL);
    }

    @Test
    void save_andCapture_updatesStatus() {
        PayPalOrder order = buildOrder("merchant_1");
        PayPalOrder saved = adapter.save(order);

        saved.markCaptured("CAPTURE-999");
        saved.pullDomainEvents();
        PayPalOrder updated = adapter.save(saved);

        assertThat(updated.getStatus()).isEqualTo(PayPalOrderStatus.CAPTURED);
        assertThat(updated.getCaptureId()).isEqualTo("CAPTURE-999");
    }

    @Test
    void findByFilters_merchantAndStatus_returnsCorrectResults() {
        PayPalOrder order = buildOrder("merchant_test");
        adapter.save(order);

        Page<PayPalOrder> result = adapter.findByFilters(
                "merchant_test", PayPalOrderStatus.PENDING_APPROVAL,
                null, null, PageRequest.of(0, 10));

        assertThat(result.getContent())
                .allMatch(o -> o.getMerchantId().equals("merchant_test"));
    }
}
