package com.mansa.infrastructure.persistence.repository;


import com.mansa.infrastructure.persistence.entity.TransactionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, UUID> {

    Optional<TransactionJpaEntity> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);

    @Query("SELECT t FROM TransactionJpaEntity t LEFT JOIN FETCH t.attempts WHERE t.id = :id")
    Optional<TransactionJpaEntity> findByIdWithAttempts(UUID id);
}
