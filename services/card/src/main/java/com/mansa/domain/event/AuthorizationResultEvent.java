package com.mansa.domain.event;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.Data;
@Data
public class AuthorizationResultEvent {
    public String transactionId;
    public String rrn;
    public boolean authorized;
    public String authCode;
    public String reason;
    public BigDecimal amount;
    public String currency;
    public Instant timestamp;

    public AuthorizationResultEvent(){}
    public AuthorizationResultEvent(String transactionId, String rrn, boolean authorized, String authCode, String reason, java.math.BigDecimal amount, String currency, Instant timestamp){
        this.transactionId=transactionId; this.rrn=rrn; this.authorized=authorized; this.authCode=authCode; this.reason=reason; this.amount=amount; this.currency=currency; this.timestamp=timestamp;
    }
}