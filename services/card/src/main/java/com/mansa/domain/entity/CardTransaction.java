package com.mansa.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
@Data
@Entity
@Table(name = "card_transactions")
public class CardTransaction {
    @Id
    private String id;
    private String panMasked;
    private BigDecimal amount;
    private String currency;
    private String merchantId;
    private String rrn;
    private String status;      // AUTHORIZED/DECLINED
    private String authCode;
    private Instant requestTime;
    private Instant responseTime;

    // getters/setters omitted for brevity
}
