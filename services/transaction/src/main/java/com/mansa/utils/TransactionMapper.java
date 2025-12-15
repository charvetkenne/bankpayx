package com.mansa.utils;


//import com.mansa.application.service.dto.CreateTransactionRequest;
import com.mansa.application.service.dto.TransactionResponse;
import com.mansa.domain.events.TransactionCreatedEvent;
import com.mansa.domain.events.TransactionFailedEvent;
import com.mansa.domain.events.TransactionProcessedEvent;
import com.mansa.domain.model.Transaction;

public final class TransactionMapper {
  public static TransactionResponse toResponse(Transaction tx) {
    TransactionResponse r = new TransactionResponse();
    r.setId(tx.getId());
    r.setSourceAccount(tx.getSourceAccount());
    r.setDestinationAccount(tx.getDestinationAccount());
    r.setAmount(tx.getAmount());
    r.setCurrency(tx.getCurrency());
    r.setStatus(tx.getStatus());
    r.setType(tx.getType());
    r.setCreatedAt(tx.getCreatedAt());
    r.setUpdatedAt(tx.getUpdatedAt());
    return r;
  }

  public static TransactionCreatedEvent toCreatedEvent(Transaction tx) {
    return new TransactionCreatedEvent(tx.getId(), tx.getAmount(), tx.getCurrency(), tx.getType(), tx.getSourceAccount(), tx.getDestinationAccount());
  }

  public static TransactionProcessedEvent toProcessedEvent(Transaction tx) {
    return new TransactionProcessedEvent(tx.getId(), tx.getStatus().name());
  }

  public static TransactionFailedEvent toFailedEvent(Transaction tx, String reason) {
    return new TransactionFailedEvent(tx.getId(), reason);
  }
}
