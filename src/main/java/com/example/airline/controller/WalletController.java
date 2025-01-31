package com.example.airline.controller;

import com.example.airline.dto.TransactionResponseDTO;
import com.example.airline.dto.WalletRequestDTO;
import com.example.airline.dto.WalletResponseDTO;
import com.example.airline.entity.Transaction;
import com.example.airline.entity.TransactionType;
import com.example.airline.entity.Wallet;
import com.example.airline.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/add-money")
    public ResponseEntity<WalletResponseDTO> addMoney(@Valid @RequestBody WalletRequestDTO request) {
        Wallet wallet = walletService.addMoney(request);
        return ResponseEntity.ok(convertToWalletDTO(wallet));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<WalletResponseDTO> getWallet(@PathVariable Long userId) {
        Wallet wallet = walletService.getWallet(userId);
        return ResponseEntity.ok(convertToWalletDTO(wallet));
    }

    @GetMapping("/{userId}/transactions")
    public ResponseEntity<List<TransactionResponseDTO>> getUserTransactions(@PathVariable Long userId) {
        List<Transaction> transactions = walletService.getUserTransactions(userId);
        List<TransactionResponseDTO> dtos = transactions.stream()
            .map(this::convertToTransactionDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    private WalletResponseDTO convertToWalletDTO(Wallet wallet) {
        WalletResponseDTO dto = new WalletResponseDTO();
        dto.setId(wallet.getId());
        dto.setUserId(wallet.getUserId());
        dto.setBalance(wallet.getBalance());
        dto.setLastUpdated(wallet.getLastUpdated());
        return dto;
    }

    private TransactionResponseDTO convertToTransactionDTO(Transaction transaction) {
        TransactionResponseDTO dto = new TransactionResponseDTO();
        dto.setId(transaction.getId());
        dto.setUserId(transaction.getUserId());
        dto.setAmount(transaction.getAmount());
        dto.setType(TransactionType.valueOf(transaction.getType().name()));
        dto.setCreatedAt(transaction.getCreatedAt());
        return dto;
    }
}