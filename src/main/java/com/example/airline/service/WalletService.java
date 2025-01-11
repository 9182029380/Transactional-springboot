package com.example.airline.service;

import com.example.airline.dto.WalletRequestDTO;
import com.example.airline.entity.Wallet;
import com.example.airline.exception.ResourceNotFoundException;
import com.example.airline.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Transactional
    public Wallet addMoney(WalletRequestDTO request) {
        Wallet wallet = walletRepository.findByUserId(request.getUserId())
                .orElse(new Wallet());

        if (wallet.getId() == null) {
            wallet.setUserId(request.getUserId());
            wallet.setBalance(0d);
        }

        wallet.setBalance(wallet.getBalance()+(request.getAmount()));
        return walletRepository.save(wallet);
    }

    public Wallet getWallet(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
    }
}
