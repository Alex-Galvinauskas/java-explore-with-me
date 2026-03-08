package ru.practicum.stats.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class StatsValidationException extends RuntimeException {

    private final HttpStatus status;
    private final String reason;

    public StatsValidationException(String message, String reason) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.reason = reason;
    }

    public StatsValidationException(String message, String reason, HttpStatus status) {
        super(message);
        this.status = status;
        this.reason = reason;
    }

    public StatsValidationException(String message, String reason, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.reason = reason;
    }
}