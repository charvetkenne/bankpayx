package com.mansa.domain.event;

import com.mansa.domain.model.Money;

public class PaymentCapturedEvent extends DomainEvent {

    private final String transactionId;
    private final Money  amount;

    public PaymentCapturedEvent(String transactionId, Money amount, String correlationId) {
        super(correlationId);
        this.transactionId = transactionId;
        this.amount        = amount;
    }

    public String getTransactionId() { return transactionId; }
    public Money  getAmount()        { return amount; }
}
