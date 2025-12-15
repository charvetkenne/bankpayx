package com.mansa.domain.model;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.Data;
//import org.hibernate.annotations.GenericGenerator;
@Data
@Entity
@Table(name = "transactions")
public class Transaction {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  private String sourceAccount;
  private String destinationAccount;

  @Column(precision = 19, scale = 4)
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

  protected Transaction() {}

  public Transaction(String sourceAccount, String destinationAccount, BigDecimal amount, String currency, TransactionType type) {
    this.sourceAccount = sourceAccount;
    this.destinationAccount = destinationAccount;
    this.amount = amount;
    this.currency = currency;
    this.type = type;
    this.status = TransactionStatus.PENDING;
    this.createdAt = Instant.now();
    this.updatedAt = Instant.now();
  }

  public UUID getId() { return id; }
  public String getSourceAccount() { return sourceAccount; }
  public String getDestinationAccount() { return destinationAccount; }
  public BigDecimal getAmount() { return amount; }
  public String getCurrency() { return currency; }
  public TransactionStatus getStatus() { return status; }
  public TransactionType getType() { return type; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }

  public void markProcessing() {
    this.status = TransactionStatus.PROCESSING;
    this.updatedAt = Instant.now();
  }

  public void markSuccess() {
    this.status = TransactionStatus.SUCCESS;
    this.updatedAt = Instant.now();
  }

  public void markFailed() {
    this.status = TransactionStatus.FAILED;
    this.updatedAt = Instant.now();
  }
}

