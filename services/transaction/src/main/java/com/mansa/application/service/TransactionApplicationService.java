package com.mansa.application.service;


import com.mansa.utils.TransactionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mansa.application.port.in.CreateTransactionCommandPort;
import com.mansa.application.port.out.EventPublisherPort;
import com.mansa.application.port.out.TransactionRepositoryPort;
import com.mansa.application.service.dto.CreateTransactionRequest;
import com.mansa.application.service.dto.TransactionResponse;
import com.mansa.domain.events.TransactionCreatedEvent;
import com.mansa.domain.model.Transaction;

import java.util.UUID;

@Service
public class TransactionApplicationService implements CreateTransactionCommandPort {

  private final TransactionRepositoryPort repository;
  private final EventPublisherPort publisher;

  public TransactionApplicationService(TransactionRepositoryPort repository, EventPublisherPort publisher) {
    this.repository = repository;
    this.publisher = publisher;
  }

  @Override
  @Transactional
  public TransactionResponse create(CreateTransactionRequest request) {
    // validations (sommaire)
    if (request.getAmount().signum() <= 0) {
      throw new IllegalArgumentException("amount must be > 0");
    }
    Transaction tx = new Transaction(
        request.getSourceAccount(),
        request.getDestinationAccount(),
        request.getAmount(),
        request.getCurrency(),
        request.getType()
    );

    Transaction saved = repository.save(tx);

    // publish domain event for downstream adapters (card/sepa/mobilemoney)
    TransactionCreatedEvent event = TransactionMapper.toCreatedEvent(saved);
    publisher.publish("transactions.created", event);

    return TransactionMapper.toResponse(saved);
  }

  // Exemple de méthode de traitement synchrone/simple
  @Transactional
  public TransactionResponse process(UUID transactionId) {
    Transaction tx = repository.findById(transactionId).orElseThrow();
    tx.markProcessing();
    repository.save(tx);

    // ici on pourrait appeler d'autres adapters sync/async (reserve funds, call ISO adapters)
    // simulation: mark success
    try {
      // logic to call external systems...
      tx.markSuccess();
      repository.save(tx);
      publisher.publish("transactions.processed", TransactionMapper.toProcessedEvent(tx));
    } catch (Exception ex) {
      tx.markFailed();
      repository.save(tx);
      publisher.publish("transactions.failed", TransactionMapper.toFailedEvent(tx, ex.getMessage()));
    }
    return TransactionMapper.toResponse(tx);
  }
}
