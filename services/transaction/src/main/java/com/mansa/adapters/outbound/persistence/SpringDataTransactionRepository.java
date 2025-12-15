package com.mansa.adapters.outbound.persistence;



import com.mansa.application.port.out.TransactionRepositoryPort;
import com.mansa.domain.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
public class SpringDataTransactionRepository implements TransactionRepositoryPort {
  private final JpaTransactionCrudRepository repo;

  public SpringDataTransactionRepository(JpaTransactionCrudRepository repo) {
    this.repo = repo;
  }

  @Override
  public Transaction save(Transaction tx) {
    TransactionJpaEntity e = TransactionJpaEntity.fromDomain(tx);
    Objects.requireNonNull(e , "it must be non null");
    TransactionJpaEntity saved = repo.save(e);
    return saved.toDomain();
  }

  @Override
  public Optional<Transaction> findById(UUID id) {
    Objects.requireNonNull(id,"it must be a non null");
    return repo.findById(id).map(TransactionJpaEntity::toDomain);
  }
}
