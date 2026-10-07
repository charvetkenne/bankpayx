package com.mansa.application.service.dto;


import com.mansa.domain.model.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
@Data
public class CreateTransactionRequest {
  @NotBlank
  private String sourceAccount;
  @NotBlank
  private String destinationAccount;
  @NotNull
  private BigDecimal amount;
  @NotBlank
  private String currency;
  @NotNull
  private TransactionType type;

  // getters / setters
}

