package com.example.airline.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WalletResponseDTO {
    private Long id;
    private Long userId;
    private Double balance;
    private LocalDateTime lastUpdated;
}
