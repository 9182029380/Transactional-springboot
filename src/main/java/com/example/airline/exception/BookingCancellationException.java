package com.example.airline.exception;

import org.springframework.http.HttpStatus;

public class BookingCancellationException extends BaseException {
    public BookingCancellationException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
