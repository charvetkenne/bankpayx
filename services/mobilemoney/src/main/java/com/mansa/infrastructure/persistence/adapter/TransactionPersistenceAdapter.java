package com.mansa.infrastructure.persistence.adapter;


import com.mansa.application.port.out.TransactionRepositoryPort;
import com.mansa.domain.aggregate.MobileMoneyTransaction;
import com.mansa.domain.entity.TransactionAttempt;
import com.mansa.domain.valueobject.*;
import com.mansa.infrastructure.persistence.entity.TransactionAttemptJpaEntity;
import com.mansa.infrastructure.persistence.entity.TransactionJpaEntity;
import com.mansa.infrastructure.persistence.repository.TransactionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionPersistenceAdapter implements TransactionRepositoryPort {

    private final TransactionJpaRepository jpaRepository;

    @Override
    public MobileMoneyTransaction save(MobileMoneyTransaction transaction) {
        TransactionJpaEntity entity = toJpaEntity(transaction);
        TransactionJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<MobileMoneyTransaction> findById(TransactionId transactionId) {
        return jpaRepository.findByIdWithAttempts(transactionId.value())
                .map(this::toDomain);
    }

    @Override
    public Optional<MobileMoneyTransaction> findByIdempotencyKey(IdempotencyKey idempotencyKey) {
        return jpaRepository.findByIdempotencyKey(idempotencyKey.value())
                .map(this::toDomain);
    }

    @Override
    public boolean existsByIdempotencyKey(IdempotencyKey idempotencyKey) {
        return jpaRepository.existsByIdempotencyKey(idempotencyKey.value());
    }

    // ---- Mapping: Domain → JPA ----

    private TransactionJpaEntity toJpaEntity(MobileMoneyTransaction tx) {
        List<TransactionAttemptJpaEntity> attemptEntities = tx.getAttempts().stream()
                .map(this::toAttemptJpaEntity)
                .toList();

        TransactionJpaEntity entity = TransactionJpaEntity.builder()
                .id(tx.getId().value())
                .idempotencyKey(tx.getIdempotencyKey().value())
                .phoneNumber(tx.getPhoneNumber().value())
                .amount(tx.getAmount().amount())
                .currency(tx.getAmount().currencyCode())
                .operatorCode(tx.getOperatorCode().name())
                .status(tx.getStatus().name())
                .operatorReference(tx.getOperatorReference() != null ? tx.getOperatorReference().value() : null)
                .customerId(tx.getCustomerId())
                .correlationId(tx.getCorrelationId())
                .failureReason(tx.getFailureReason())
                .failureCode(tx.getFailureCode())
                .cancellationReason(tx.getCancellationReason())
                .retryCount(tx.getRetryCount())
                .completedAt(tx.getCompletedAt())
                .build();

        entity.setAttempts(attemptEntities);
        return entity;
    }

    private TransactionAttemptJpaEntity toAttemptJpaEntity(TransactionAttempt attempt) {
        return TransactionAttemptJpaEntity.builder()
                .id(attempt.getId())
                .transactionId(attempt.getTransactionId())
                .operatorCode(attempt.getOperatorCode().name())
                .attemptNumber(attempt.getAttemptNumber())
                .attemptedAt(attempt.getAttemptedAt())
                .resultCode(attempt.getResultCode())
                .resultMessage(attempt.getResultMessage())
                .successful(attempt.isSuccessful())
                .build();
    }

    // ---- Mapping: JPA → Domain ----

    private MobileMoneyTransaction toDomain(TransactionJpaEntity entity) {
        List<TransactionAttempt> attempts = entity.getAttempts().stream()
                .map(this::toAttemptDomain)
                .toList();

        return new MobileMoneyTransaction(
                TransactionId.of(entity.getId()),
                IdempotencyKey.of(entity.getIdempotencyKey()),
                PhoneNumber.of(entity.getPhoneNumber()),
                Money.of(entity.getAmount(), entity.getCurrency()),
                OperatorCode.valueOf(entity.getOperatorCode()),
                TransactionStatus.valueOf(entity.getStatus()),
                entity.getOperatorReference() != null
                        ? OperatorReference.of(entity.getOperatorReference()) : null,
                entity.getCustomerId(),
                entity.getCorrelationId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getCompletedAt(),
                entity.getFailureReason(),
                entity.getFailureCode(),
                entity.getCancellationReason(),
                entity.getRetryCount(),
                attempts
        );
    }

    private TransactionAttempt toAttemptDomain(TransactionAttemptJpaEntity entity) {
        return new TransactionAttempt(
                entity.getId(),
                entity.getTransactionId(),
                OperatorCode.valueOf(entity.getOperatorCode()),
                entity.getAttemptNumber(),
                entity.getAttemptedAt(),
                entity.getResultCode(),
                entity.getResultMessage(),
                entity.isSuccessful()
        );
    }
}