package com.mansa.domain.event;

import com.mansa.domain.model.Money;

public class PaymentAuthorizedEvent extends DomainEvent {

    private final String transactionId;
    private final Money  amount;

    public PaymentAuthorizedEvent(String transactionId, Money amount, String correlationId) {
        super(correlationId);
        this.transactionId = transactionId;
        this.amount        = amount;
    }

    public String getTransactionId() { return transactionId; }
    public Money  getAmount()        { return amount; }
}
