package com.mansa.infrastructure.paypal.persistence.repository;

import com.mansa.domain.enums.PayPalOrderStatus;
import com.mansa.infrastructure.paypal.persistence.entity.PayPalOrderEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface PayPalOrderJpaRepository extends JpaRepository<PayPalOrderEntity, String> {
    Optional<PayPalOrderEntity> findByPaypalOrderId(String paypalOrderId);
    Page<PayPalOrderEntity> findByMerchantId(String merchantId, Pageable pageable);

    @Query("""
        SELECT p FROM PayPalOrderEntity p
        WHERE (:merchantId IS NULL OR p.merchantId = :merchantId)
          AND (:status     IS NULL OR p.status     = :status)
          AND (:from       IS NULL OR p.createdAt >= :from)
          AND (:to         IS NULL OR p.createdAt <= :to)
        ORDER BY p.createdAt DESC
        """)
    Page<PayPalOrderEntity> findByFilters(
            @Param("merchantId") String            merchantId,
            @Param("status")     PayPalOrderStatus  status,
            @Param("from")       Instant            from,
            @Param("to")         Instant            to,
            Pageable pageable
    ); 
}
