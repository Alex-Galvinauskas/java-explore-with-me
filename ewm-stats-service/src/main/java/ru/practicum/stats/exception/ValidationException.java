package ru.practicum.stats.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends RuntimeException {
    private final HttpStatus status = HttpStatus.BAD_REQUEST;

    public ValidationException(String message) {
        super(message);
    }

    public HttpStatus getStatus() {
        return status;
    }
}