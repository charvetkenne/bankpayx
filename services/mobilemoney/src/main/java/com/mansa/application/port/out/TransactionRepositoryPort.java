package com.mansa.application.port.out;


import com.mansa.domain.aggregate.MobileMoneyTransaction;
import com.mansa.domain.valueobject.IdempotencyKey;
import com.mansa.domain.valueobject.TransactionId;

import java.util.Optional;

public interface TransactionRepositoryPort {

    MobileMoneyTransaction save(MobileMoneyTransaction transaction);

    Optional<MobileMoneyTransaction> findById(TransactionId transactionId);

    Optional<MobileMoneyTransaction> findByIdempotencyKey(IdempotencyKey idempotencyKey);

    boolean existsByIdempotencyKey(IdempotencyKey idempotencyKey);
}
