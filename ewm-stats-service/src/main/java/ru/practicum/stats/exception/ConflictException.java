package ru.practicum.stats.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ConflictException extends RuntimeException {
    private final HttpStatus status = HttpStatus.CONFLICT;

    public ConflictException(String message) {
        super(message);
    }

}