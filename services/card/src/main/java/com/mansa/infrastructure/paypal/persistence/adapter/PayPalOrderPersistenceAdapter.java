package com.mansa.infrastructure.paypal.persistence.adapter;

import com.mansa.application.port.out.PayPalOrderPersistencePort;
import com.mansa.domain.enums.PayPalOrderStatus;
import com.mansa.domain.model.PayPalOrder;
import com.mansa.infrastructure.paypal.persistence.mapper.PayPalOrderMapper;
import com.mansa.infrastructure.paypal.persistence.repository.PayPalOrderJpaRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PayPalOrderPersistenceAdapter implements PayPalOrderPersistencePort {

    private final PayPalOrderJpaRepository repository;
    private final PayPalOrderMapper        mapper;

    @Override
    public PayPalOrder save(PayPalOrder order) {
        return mapper.toDomain(repository.save(mapper.toEntity(order)));
    }

    @Override
    public Optional<PayPalOrder> findByTransactionId(String transactionId) {
        return repository.findById(transactionId).map(mapper::toDomain);
    }

    @Override
    public Optional<PayPalOrder> findByPaypalOrderId(String paypalOrderId) {
        return repository.findByPaypalOrderId(paypalOrderId).map(mapper::toDomain);
    }
     @Override public Page<PayPalOrder> findAll(Pageable p) {
        return repository.findAll(p).map(mapper::toDomain);
    }
    @Override public Page<PayPalOrder> findByMerchantId(String merchantId, Pageable p) {
        return repository.findByMerchantId(merchantId, p).map(mapper::toDomain);
    }
    @Override public Page<PayPalOrder> findByFilters(String merchantId, PayPalOrderStatus status,
                                                     Instant from, Instant to, Pageable p) {
        return repository.findByFilters(merchantId, status, from, to, p).map(mapper::toDomain);
    }
}
