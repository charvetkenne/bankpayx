package com.mansa.adapters.outbound.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data CRUD repository pour TransactionJpaEntity.
 * Le service d'adaptation (SpringDataTransactionRepository) l'utilise pour persister les entités JPA.
 */
@Repository
public interface JpaTransactionCrudRepository extends JpaRepository<TransactionJpaEntity, UUID> {
    // méthodes custom (si nécessaire) :
    // Optional<TransactionJpaEntity> findBySomeField(String someField);
}

