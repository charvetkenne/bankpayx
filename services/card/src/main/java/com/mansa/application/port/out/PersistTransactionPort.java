package com.mansa.application.port.out;
import com.mansa.domain.entity.CardTransaction;

public interface PersistTransactionPort {
    CardTransaction save(CardTransaction tx);
    CardTransaction findById(String id);
}
