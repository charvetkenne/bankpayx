package com.mansa.adapters.outbound.persistence;


import com.mansa.domain.model.TransactionStatus;
import com.mansa.domain.model.TransactionType;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
@Data
@Entity
@Table(name = "transactions")
public class TransactionJpaEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;
  private String sourceAccount;
  private String destinationAccount;
  private BigDecimal amount;
  private String currency;
  @Enumerated(EnumType.STRING)
  private TransactionStatus status;
  @Enumerated(EnumType.STRING)
  private TransactionType type;
  private Instant createdAt;
  private Instant updatedAt;
  @Version
  private Long version;

  // getters/setters
  // mapping helpers
  public static TransactionJpaEntity fromDomain(com.mansa.domain.model.Transaction tx) {
    TransactionJpaEntity e = new TransactionJpaEntity();
    e.id = tx.getId();
    e.sourceAccount = tx.getSourceAccount();
    e.destinationAccount = tx.getDestinationAccount();
    e.amount = tx.getAmount();
    e.currency = tx.getCurrency();
    e.status = tx.getStatus();
    e.type = tx.getType();
    e.createdAt = tx.getCreatedAt();
    e.updatedAt = tx.getUpdatedAt();
    e.version = tx.getVersion();
    return e;
  }
  public com.mansa.domain.model.Transaction toDomain() {
    com.mansa.domain.model.Transaction t = new com.mansa.domain.model.Transaction(
      sourceAccount, destinationAccount, amount, currency, type
    );
    // reflection/setting id/version omitted for brevity; prefer using constructor + setters if needed
    return t;
  }
}
