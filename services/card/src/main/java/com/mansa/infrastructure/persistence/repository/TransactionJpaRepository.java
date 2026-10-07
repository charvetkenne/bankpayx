package com.mansa.infrastructure.persistence.repository;

import com.mansa.domain.enums.PaymentStatus;
import com.mansa.infrastructure.persistence.entity.TransactionEntity;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.time.Instant;
import java.util.List;

@Repository
public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, String> {

    Optional<TransactionEntity> findByGatewayTransactionId(String gatewayTransactionId);

   // List<TransactionEntity> findByMerchantId(String merchantId);

    List<TransactionEntity> findByMerchantIdAndStatus(String merchantId, PaymentStatus status);
     Page<TransactionEntity> findByMerchantId(String merchantId, Pageable pageable);

    @Query("""
        SELECT t FROM TransactionEntity t
        WHERE (:merchantId IS NULL OR t.merchantId = :merchantId)
          AND (:status     IS NULL OR t.status     = :status)
          AND (:from       IS NULL OR t.createdAt >= :from)
          AND (:to         IS NULL OR t.createdAt <= :to)
        ORDER BY t.createdAt DESC
        """)
    Page<TransactionEntity> findByFilters(
            @Param("merchantId") String        merchantId,
            @Param("status")     PaymentStatus status,
            @Param("from")       Instant       from,
            @Param("to")         Instant       to,
            Pageable pageable
    );
}
