package com.mansa.dto;


import java.math.BigDecimal;

public class AuthorizationRequestDTO {
    public String pan;
    public String expiry;    // YYMM
    public BigDecimal amount;
    public String currency;
    public String merchantId;
    public String rrn;
    // Add validation annotations in production (@NotNull, @Pattern...)
}
