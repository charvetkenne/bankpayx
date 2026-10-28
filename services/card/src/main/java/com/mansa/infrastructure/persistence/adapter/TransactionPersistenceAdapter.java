package com.mansa.infrastructure.persistence.adapter;

import com.mansa.application.port.out.TransactionPersistencePort;
import com.mansa.domain.enums.PaymentStatus;
import com.mansa.domain.model.CardPayment;
import com.mansa.infrastructure.persistence.mapper.TransactionMapper;
import com.mansa.infrastructure.persistence.repository.TransactionJpaRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TransactionPersistenceAdapter implements TransactionPersistencePort {

    private final TransactionJpaRepository repository;
    private final TransactionMapper        mapper;

    @Override
    public CardPayment save(CardPayment payment) {
        var entity = mapper.toEntity(payment);
        var saved  = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<CardPayment> findByTransactionId(String transactionId) {
        return repository.findById(transactionId).map(mapper::toDomain);
    }

    @Override
    public Optional<CardPayment> findByGatewayTransactionId(String gatewayTransactionId) {
        return repository.findByGatewayTransactionId(gatewayTransactionId).map(mapper::toDomain);
    }
    @Override public Page<CardPayment> findAll(Pageable p) {
        return repository.findAll(p).map(mapper::toDomain);
    }
    @Override public Page<CardPayment> findByMerchantId(String merchantId, Pageable p) {
        return repository.findByMerchantId(merchantId, p).map(mapper::toDomain);
    }
    @Override public Page<CardPayment> findByFilters(String merchantId, PaymentStatus status,
                                                     Instant from, Instant to, Pageable p) {
        return repository.findByFilters(merchantId, status, from, to, p).map(mapper::toDomain);
    }

}
