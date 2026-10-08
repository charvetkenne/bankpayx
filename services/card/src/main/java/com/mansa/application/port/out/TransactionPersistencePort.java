package com.mansa.application.port.out;

import com.mansa.domain.enums.PaymentStatus;
import com.mansa.domain.model.CardPayment;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionPersistencePort {

    CardPayment save(CardPayment payment);

    Optional<CardPayment> findByTransactionId(String transactionId);

    Optional<CardPayment> findByGatewayTransactionId(String gatewayTransactionId);
     Page<CardPayment> findAll(Pageable pageable);

    Page<CardPayment> findByMerchantId(String merchantId, Pageable pageable);

    Page<CardPayment> findByFilters(String merchantId, PaymentStatus status,
                                    Instant from, Instant to, org.springframework.data.domain.Pageable pageable);
}
