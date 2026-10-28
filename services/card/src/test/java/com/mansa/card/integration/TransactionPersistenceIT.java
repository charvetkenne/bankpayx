package com.mansa.card.integration;

import com.mansa.domain.enums.Currency;
import com.mansa.domain.enums.PaymentStatus;
import com.mansa.domain.model.CardPayment;
import com.mansa.domain.model.Money;
import com.mansa.infrastructure.persistence.adapter.TransactionPersistenceAdapter;
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
@Import({TransactionPersistenceAdapter.class,
         com.mansa.infrastructure.persistence.mapper.TransactionMapper.class})
class TransactionPersistenceIT {

    @Autowired TransactionPersistenceAdapter adapter;

    private CardPayment buildPayment(String merchantId) {
        CardPayment payment = CardPayment.initiate(
                Money.of(50.00, Currency.EUR),
                merchantId, "Test payment", "corr-001", "pm_test"
        );
        payment.markAuthorized("pi_test_001");
        payment.pullDomainEvents();
        return payment;
    }

    @Test
    void save_andFindByTransactionId_returnsCorrectPayment() {
        CardPayment saved = adapter.save(buildPayment("merchant_1"));

        Optional<CardPayment> found = adapter.findByTransactionId(saved.getTransactionId());

        assertThat(found).isPresent();
        assertThat(found.get().getMerchantId()).isEqualTo("merchant_1");
        assertThat(found.get().getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
    }

    @Test
    void findAll_returnsPaginatedResults() {
        adapter.save(buildPayment("merchant_1"));
        adapter.save(buildPayment("merchant_1"));
        adapter.save(buildPayment("merchant_2"));

        Page<CardPayment> page = adapter.findAll(PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(3);
    }

    @Test
    void findByFilters_merchantIdFilter_returnsOnlyMatchingPayments() {
        adapter.save(buildPayment("merchant_A"));
        adapter.save(buildPayment("merchant_B"));

        Page<CardPayment> result = adapter.findByFilters(
                "merchant_A", null, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent())
                .allMatch(p -> p.getMerchantId().equals("merchant_A"));
    }

    @Test
    void findByFilters_statusFilter_returnsOnlyMatchingStatus() {
        CardPayment authorized = buildPayment("merchant_1");
        adapter.save(authorized);

        Page<CardPayment> result = adapter.findByFilters(
                null, PaymentStatus.AUTHORIZED, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent())
                .allMatch(p -> p.getStatus() == PaymentStatus.AUTHORIZED);
    }
}
