package com.mansa.application.port.in;

import com.mansa.application.service.dto.CreateTransactionRequest;
import com.mansa.application.service.dto.TransactionResponse;

public interface CreateTransactionCommandPort {
  TransactionResponse create(CreateTransactionRequest request);
}
