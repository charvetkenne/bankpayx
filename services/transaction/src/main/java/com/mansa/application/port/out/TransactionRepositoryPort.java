package com.mansa.application.port.out;


import com.mansa.domain.model.Transaction;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepositoryPort {
  Transaction save(Transaction tx);
  Optional<Transaction> findById(UUID id);
}