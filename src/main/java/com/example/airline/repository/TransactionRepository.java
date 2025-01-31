package com.example.airline.repository;

import com.example.airline.entity.Transaction;
import com.example.airline.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Transaction> findByWalletIdOrderByCreatedAtDesc(Long walletId);
    List<Transaction> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, TransactionType type);
}
