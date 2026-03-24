package ru.practicum.stats.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ValidationException extends RuntimeException {
    private final HttpStatus status = HttpStatus.BAD_REQUEST;

    public ValidationException(String message) {
        super(message);
    }

}