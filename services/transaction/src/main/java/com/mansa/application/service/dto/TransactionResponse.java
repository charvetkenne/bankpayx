package com.mansa.application.service.dto;


import com.mansa.domain.model.TransactionStatus;
import com.mansa.domain.model.TransactionType;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
@Data
public class TransactionResponse {
  private UUID id;
  private String sourceAccount;
  private String destinationAccount;
  private BigDecimal amount;
  private String currency;
  private TransactionStatus status;
  private TransactionType type;
  private Instant createdAt;
  private Instant updatedAt;
  // getters / setters / constructor
}

