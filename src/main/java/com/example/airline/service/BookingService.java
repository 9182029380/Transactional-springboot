package com.example.airline.service;

import com.example.airline.dto.BookingRequestDTO;
import com.example.airline.entity.*;
import com.example.airline.exception.*;
import com.example.airline.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final FlightService flightService;
    private final PaymentService paymentService;
    private final UserService userService;

    public BookingService(
            BookingRepository bookingRepository,
            FlightService flightService,
            PaymentService paymentService,
            UserService userService
    ) {
        this.bookingRepository = bookingRepository;
        this.flightService = flightService;
        this.paymentService = paymentService;
        this.userService = userService;
    }

    @Transactional
    public Booking createBooking(BookingRequestDTO request) {
        // Validate user and flight
        User user = userService.getUser(request.getUserId());
        Flight flight = flightService.getFlight(request.getFlightId());

        // Check seat availability
        if (flight.getAvailableSeats() < request.getNumberOfSeats()) {
            throw new ResourceNotFoundException("Not enough seats available");
        }

        Double totalAmount = flight.getBasePrice() * request.getNumberOfSeats();

        // Create booking
        Booking booking = new Booking();
        booking.setUserId(user.getId());
        booking.setFlightId(flight.getId());
        booking.setNumberOfSeats(request.getNumberOfSeats());
        booking.setTotalAmount(totalAmount);
        booking.setStatus(Booking.BookingStatus.PENDING);

        booking = bookingRepository.save(booking);

        try {
            // Process payment
            Payment payment = paymentService.processPayment(
                    booking.getId(),
                    user.getId(),
                    totalAmount
            );

            if (payment.getStatus() == Payment.PaymentStatus.SUCCESS) {
                // Update flight seats
                flight.setAvailableSeats(flight.getAvailableSeats() - request.getNumberOfSeats());
                flightService.updateFlightStatus(flight.getId(), flight.getStatus());

                // Confirm booking
                booking.setStatus(Booking.BookingStatus.CONFIRMED);
                booking = bookingRepository.save(booking);
            } else {
                booking.setStatus(Booking.BookingStatus.FAILED);
                bookingRepository.save(booking);
                throw new PaymentFailedException("Payment failed for booking");
            }
        } catch (PaymentFailedException e) {
            booking.setStatus(Booking.BookingStatus.FAILED);
            bookingRepository.save(booking);
            throw e;
        } catch (Exception e) {
            booking.setStatus(Booking.BookingStatus.FAILED);
            bookingRepository.save(booking);
            throw new PaymentFailedException("Payment failed for booking", e);
        }

        return booking;
    }

    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new InvalidBookingStateException("Booking cannot be cancelled in current state: " + booking.getStatus());
        }

        Flight flight = flightService.getFlight(booking.getFlightId());
        validateCancellationDeadline(flight.getDepartureTime());
        Double refundAmount = calculateRefundAmount(booking, flight.getDepartureTime());

        try {
            // Process refund if applicable
            if (refundAmount > 0) {
                Payment payment = paymentService.findPaymentByBookingId(booking.getId());
                if (payment != null && payment.getStatus() == Payment.PaymentStatus.SUCCESS) {
                    paymentService.refundPayment(payment.getId(), refundAmount);
                }
            }

            // Update flight seats
            flight.setAvailableSeats(flight.getAvailableSeats() + booking.getNumberOfSeats());
            flightService.updateFlightStatus(flight.getId(), flight.getStatus());

            // Update booking status
            booking.setStatus(Booking.BookingStatus.CANCELLED);
            return bookingRepository.save(booking);

        } catch (PaymentFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new BookingCancellationException("Failed to cancel booking: " + e.getMessage());
        }
    }

    private void validateCancellationDeadline(LocalDateTime departureTime) {
        LocalDateTime now = LocalDateTime.now();

        // Check if flight has already departed
        if (now.isAfter(departureTime)) {
            throw new InvalidBookingStateException("Cannot cancel booking after flight departure time");
        }

        // Calculate time until departure
        Duration timeUntilDeparture = Duration.between(now, departureTime);
        long daysUntilDeparture = timeUntilDeparture.toDays();

        if (daysUntilDeparture <= 0) {
            throw new InvalidBookingStateException("Same day cancellations are not allowed");
        }
    }

    private Double calculateRefundAmount(Booking booking, LocalDateTime departureTime) {
        LocalDateTime now = LocalDateTime.now();
        Duration timeUntilDeparture = Duration.between(now, departureTime);

        // Refund policy based on time until departure
        if (timeUntilDeparture.toDays() > 7) {
            // Full refund if cancelled more than 7 days before
            return booking.getTotalAmount();
        } else if (timeUntilDeparture.toDays() > 3) {
            // 75% refund if cancelled 3-7 days before
            return booking.getTotalAmount() * 0.75;
        } else if (timeUntilDeparture.toDays() > 1) {
            // 50% refund if cancelled 1-3 days before
            return booking.getTotalAmount() * 0.50;
        } else {
            // No refund if cancelled less than 24 hours before
            return 0.0;
        }
    }
}