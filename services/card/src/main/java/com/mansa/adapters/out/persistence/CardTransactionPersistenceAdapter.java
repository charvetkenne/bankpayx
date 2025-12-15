package com.mansa.adapters.out.persistence;
import com.mansa.application.port.out.PersistTransactionPort;
import com.mansa.domain.entity.CardTransaction;

import java.util.Objects;

import org.springframework.stereotype.Component;

@Component
public class CardTransactionPersistenceAdapter implements PersistTransactionPort {
    private final CardTransactionJpaRepository jpa;
    public CardTransactionPersistenceAdapter(CardTransactionJpaRepository jpa){ this.jpa = jpa; }
    @Override public CardTransaction save(CardTransaction tx){ 
        Objects.requireNonNull(tx ,"it must be a non null");
        return jpa.save(tx); }
    @Override public CardTransaction findById(String id){ 
        Objects.requireNonNull(id,"it must be a non null");
        return jpa.findById(id).orElse(null); }
}
