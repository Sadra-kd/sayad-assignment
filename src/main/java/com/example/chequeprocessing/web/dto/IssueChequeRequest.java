package com.example.chequeprocessing.web.dto;

import java.math.BigDecimal;

public class IssueChequeRequest {
    private Long drawerId;
    private String number;
    private BigDecimal amount;

    public Long getDrawerId() { return drawerId; }
    public void setDrawerId(Long drawerId) { this.drawerId = drawerId; }
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}


