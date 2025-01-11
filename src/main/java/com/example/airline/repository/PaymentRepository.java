package com.example.airline.repository;

import com.example.airline.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByBookingId(Long bookingId);
    Optional<Payment> findByBookingIdAndStatus(Long bookingId, Payment.PaymentStatus status);
}