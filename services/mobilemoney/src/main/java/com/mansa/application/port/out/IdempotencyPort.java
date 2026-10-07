package com.mansa.application.port.out;


import com.mansa.domain.valueobject.IdempotencyKey;
import com.mansa.domain.valueobject.TransactionId;

import java.util.Optional;

public interface IdempotencyPort {

    /**
     * Atomically checks if a key exists, and if not, stores it.
     * Returns the existing TransactionId if the key was already stored.
     */
    Optional<TransactionId> getIfPresent(IdempotencyKey key);

    void store(IdempotencyKey key, TransactionId transactionId, long ttlSeconds);

    void evict(IdempotencyKey key);
}
