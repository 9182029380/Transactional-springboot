package com.example.airline.exception;

import org.springframework.http.HttpStatus;

public class RefundFailedException extends BaseException {
    public RefundFailedException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
