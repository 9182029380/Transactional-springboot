package com.example.airline.dto;

import lombok.Data;
import java.math.BigDecimal;

//@Data
public class WalletRequestDTO {
    private Long userId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    private Double amount;
}
