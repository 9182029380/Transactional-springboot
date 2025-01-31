package com.example.airline.service;

import com.example.airline.dto.WalletRequestDTO;
import com.example.airline.entity.Transaction;
import com.example.airline.entity.TransactionType;
import com.example.airline.entity.Wallet;
import com.example.airline.exception.InsufficientBalanceException;
import com.example.airline.exception.ResourceNotFoundException;
import com.example.airline.repository.TransactionRepository;
import com.example.airline.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public WalletService(WalletRepository walletRepository, TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Wallet addMoney(WalletRequestDTO request) {
        return addMoney(request.getUserId(), request.getAmount(), "Wallet top-up");
    }

    @Transactional
    public Wallet addMoney(Long userId, Double amount, String description) {
        Wallet wallet = getWallet(userId);
        wallet.setBalance(wallet.getBalance() + amount);
        wallet = walletRepository.save(wallet);

        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setWallet(wallet);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.CREDIT);
        transaction.setDescription(description);
        transaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        return wallet;
    }

    @Transactional
    public Wallet deductMoney(Long userId, Double amount, String description) {
        Wallet wallet = getWallet(userId);
        
        if (wallet.getBalance() < amount) {
            throw new InsufficientBalanceException("Insufficient balance in wallet");
        }

        wallet.setBalance(wallet.getBalance() - amount);
        wallet = walletRepository.save(wallet);

        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setWallet(wallet);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.DEBIT);
        transaction.setDescription(description);
        transaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        return wallet;
    }

    public Wallet getWallet(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user: " + userId));
    }

    public List<Transaction> getUserTransactions(Long userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Transaction> getUserTransactionsByType(Long userId, TransactionType type) {
        return transactionRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type);
    }

    public List<Transaction> getWalletTransactions(Long walletId) {
        return transactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId);
    }
}
