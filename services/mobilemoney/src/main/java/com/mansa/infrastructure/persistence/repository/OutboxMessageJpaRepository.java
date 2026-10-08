package com.mansa.infrastructure.persistence.repository;


import com.mansa.infrastructure.persistence.entity.OutboxMessageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxMessageJpaRepository extends JpaRepository<OutboxMessageJpaEntity, UUID> {

    @Query("""
            SELECT o FROM OutboxMessageJpaEntity o
            WHERE o.status = 'PENDING'
            ORDER BY o.createdAt ASC
            LIMIT :limit
            """)
    List<OutboxMessageJpaEntity> findPendingMessages(int limit);

    @Modifying
    @Query("UPDATE OutboxMessageJpaEntity o SET o.status = 'PUBLISHED', o.publishedAt = CURRENT_TIMESTAMP WHERE o.id = :id")
    void markAsPublished(UUID id);

    @Modifying
    @Query("""
            UPDATE OutboxMessageJpaEntity o
            SET o.status = 'FAILED', o.failedAt = CURRENT_TIMESTAMP, o.errorMessage = :errorMessage
            WHERE o.id = :id
            """)
    void markAsFailed(UUID id, String errorMessage);
}