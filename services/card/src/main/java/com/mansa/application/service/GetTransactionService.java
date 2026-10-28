package com.mansa.application.service;

import com.mansa.application.port.in.GetTransactionUseCase;
import com.mansa.application.port.out.TransactionPersistencePort;
import com.mansa.domain.exception.PaymentNotFoundException;
import com.mansa.domain.model.CardPayment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetTransactionService implements GetTransactionUseCase {

    private final TransactionPersistencePort persistencePort;

    @Override
    @Transactional(readOnly = true)
    public CardPayment getByTransactionId(String transactionId) {
        return persistencePort.findByTransactionId(transactionId)
                .orElseThrow(() -> new PaymentNotFoundException(transactionId));
    }
}
