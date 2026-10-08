package com.mansa.infrastructure.persistence.repository;


import com.mansa.infrastructure.persistence.entity.TransactionAttemptJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionAttemptJpaRepository extends JpaRepository<TransactionAttemptJpaEntity, UUID> {

    List<TransactionAttemptJpaEntity> findByTransactionIdOrderByAttemptNumberAsc(UUID transactionId);
}