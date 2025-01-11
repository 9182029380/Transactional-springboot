package com.example.airline.service;

import com.example.airline.entity.Payment;
import com.example.airline.entity.Wallet;
import com.example.airline.exception.InsufficientBalanceException;
import com.example.airline.exception.InvalidPaymentStateException;
import com.example.airline.exception.RefundFailedException;
import com.example.airline.exception.ResourceNotFoundException;
import com.example.airline.repository.PaymentRepository;
import com.example.airline.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final WalletRepository walletRepository;
    // PaymentService.java (continued)
    public PaymentService(
            PaymentRepository paymentRepository,
            WalletRepository walletRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.walletRepository = walletRepository;
    }

    @Transactional
    public Payment processPayment(Long bookingId, Long userId, Double amount) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance in wallet");
        }

        Payment payment = new Payment();
        payment.setBookingId(bookingId);
        payment.setUserId(userId);
        payment.setAmount(amount);
        payment.setMode(Payment.PaymentMode.WALLET);
        payment.setStatus(Payment.PaymentStatus.PENDING);

        try {
            // Deduct from wallet
            wallet.setBalance(wallet.getBalance()-(amount));
            walletRepository.save(wallet);

            // Update payment status
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
        } catch (Exception e) {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            throw e;
        }

        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
            throw new RuntimeException("Cannot refund non-successful payment");
        }

        Wallet wallet = walletRepository.findByUserId(payment.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        // Add amount back to wallet
        wallet.setBalance(wallet.getBalance()+(payment.getAmount()));
        walletRepository.save(wallet);

        // Update payment status
        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        return paymentRepository.save(payment);
    }
    public Payment findPaymentByBookingId(Long bookingId) {
        return paymentRepository.findByBookingIdAndStatus(bookingId, Payment.PaymentStatus.SUCCESS)
                .orElse(null);
    }

    @Transactional
    public Payment processRefund(Long paymentId, Double refundAmount) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
            throw new InvalidPaymentStateException("Cannot refund payment in current state: " + payment.getStatus());
        }

        Payment refund = new Payment();
        refund.setBookingId(payment.getBookingId());
        refund.setUserId(payment.getUserId());
        refund.setAmount(-refundAmount);  // Negative amount for refund
        refund.setMode(Payment.PaymentMode.WALLET);
        refund.setStatus(Payment.PaymentStatus.PENDING);

        try {
            // Add amount back to wallet
            Wallet wallet = walletRepository.findByUserId(payment.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

            wallet.setBalance(wallet.getBalance() + refundAmount);
            walletRepository.save(wallet);

            // Update refund status
            refund.setStatus(Payment.PaymentStatus.SUCCESS);
            refund = paymentRepository.save(refund);

            // Update original payment status
            payment.setStatus(Payment.PaymentStatus.REFUNDED);
            paymentRepository.save(payment);

            return refund;
        } catch (Exception e) {
            throw new RefundFailedException("Failed to process refund: " + e.getMessage());
        }
    }
}