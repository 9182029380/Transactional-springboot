package com.example.airline.dto;

import com.example.airline.entity.TransactionType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TransactionResponseDTO {
    private Long id;
    private Long userId;
    private Double amount;
    private TransactionType type;
    private String description;
    private LocalDateTime createdAt;
    
}
