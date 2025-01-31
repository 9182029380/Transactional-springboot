package com.example.airline.service;

import com.example.airline.entity.Payment;
import com.example.airline.entity.Wallet;
import com.example.airline.exception.InsufficientBalanceException;
import com.example.airline.exception.InvalidPaymentStateException;
import com.example.airline.exception.RefundFailedException;
import com.example.airline.exception.ResourceNotFoundException;
import com.example.airline.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final WalletService walletService;

    public PaymentService(
            PaymentRepository paymentRepository,
            WalletService walletService
    ) {
        this.paymentRepository = paymentRepository;
        this.walletService = walletService;
    }

    @Transactional
    public Payment processPayment(Long bookingId, Long userId, Double amount) {
        // First check if user has sufficient balance
        Wallet wallet = walletService.getWallet(userId);
        if (wallet.getBalance() < amount) {
            throw new InsufficientBalanceException("Insufficient balance in wallet");
        }

        Payment payment = new Payment();
        payment.setBookingId(bookingId);
        payment.setUserId(userId);
        payment.setAmount(amount);
        payment.setMode(Payment.PaymentMode.WALLET);
        payment.setStatus(Payment.PaymentStatus.PENDING);

        try {
            // Deduct from wallet and create DEBIT transaction
            walletService.deductMoney(userId, amount, "Payment for booking " + bookingId);
            
            // Update payment status
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
        } catch (Exception e) {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            throw e;
        }

        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment refundPayment(Long paymentId, Double refundAmount) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
            throw new InvalidPaymentStateException("Cannot refund payment in current state: " + payment.getStatus());
        }

        try {
            // Add refund amount to wallet and create CREDIT transaction
            walletService.addMoney(payment.getUserId(), refundAmount, "Refund for booking " + payment.getBookingId());
            
            // Update payment status
            payment.setStatus(Payment.PaymentStatus.REFUNDED);
            return paymentRepository.save(payment);
        } catch (Exception e) {
            throw new RefundFailedException("Failed to process refund: " + e.getMessage());
        }
    }

    public Payment findPaymentByBookingId(Long bookingId) {
        return paymentRepository.findByBookingIdAndStatus(bookingId, Payment.PaymentStatus.SUCCESS)
                .orElse(null);
    }
}