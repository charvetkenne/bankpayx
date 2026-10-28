package com.mansa.application.service;

import com.mansa.application.port.in.GetCardTransactionHistoryUseCase;
import com.mansa.application.port.out.TransactionPersistencePort;
import com.mansa.domain.model.CardPayment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetCardTransactionHistoryService implements GetCardTransactionHistoryUseCase {

    private final TransactionPersistencePort persistencePort;

    @Override
    @Transactional(readOnly = true)
    public Page<CardPayment> getHistory(Filter filter, Pageable pageable) {
        if (filter.merchantId() == null && filter.status() == null
                && filter.from() == null && filter.to() == null) {
            return persistencePort.findAll(pageable);
        }
        return persistencePort.findByFilters(
                filter.merchantId(), filter.status(),
                filter.from(), filter.to(), pageable);
    }
}
   