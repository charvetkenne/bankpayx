package com.mansa.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mansa.domain.entity.CardTransaction;

public interface CardTransactionJpaRepository extends JpaRepository<CardTransaction, String> {}