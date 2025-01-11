package com.example.airline.exception;

import org.springframework.http.HttpStatus;

public class InvalidBookingStateException extends BaseException {
    public InvalidBookingStateException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
