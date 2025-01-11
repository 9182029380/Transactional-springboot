package com.example.airline.exception;

import org.springframework.http.HttpStatus;

public class CancellationDeadlineException extends BaseException {
    public CancellationDeadlineException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
