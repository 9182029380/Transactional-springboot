package com.example.airline.exception;

import org.springframework.http.HttpStatus;

public class InsufficientBalanceException extends BaseException {
    public InsufficientBalanceException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
