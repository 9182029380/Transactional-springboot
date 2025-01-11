package com.example.airline.controller;

import com.example.airline.dto.WalletRequestDTO;
import com.example.airline.entity.Wallet;
import com.example.airline.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/add-money")
    public ResponseEntity<Wallet> addMoney(@RequestBody WalletRequestDTO request) {
        return ResponseEntity.ok(walletService.addMoney(request));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Wallet> getWallet(@PathVariable Long userId) {
        return ResponseEntity.ok(walletService.getWallet(userId));
    }
}