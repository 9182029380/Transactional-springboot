package com.example.airline.exception;

import org.springframework.http.HttpStatus;

public class InvalidPaymentStateException extends BaseException {
    public InvalidPaymentStateException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
