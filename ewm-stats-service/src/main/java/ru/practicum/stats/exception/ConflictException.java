package ru.practicum.stats.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends RuntimeException {
    private final HttpStatus status = HttpStatus.CONFLICT;

    public ConflictException(String message) {
        super(message);
    }

    public HttpStatus getStatus() {
        return status;
    }
}