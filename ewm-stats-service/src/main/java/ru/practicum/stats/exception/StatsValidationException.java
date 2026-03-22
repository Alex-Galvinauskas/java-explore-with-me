package ru.practicum.stats.exception;

import org.springframework.http.HttpStatus;

public class StatsValidationException extends RuntimeException {

    private final HttpStatus status;
    private final String reason;

    public StatsValidationException(String message, String reason) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.reason = reason;
    }

    // Конструктор для BadRequestException
    public StatsValidationException(String message, String reason, HttpStatus status) {
        super(message);
        this.status = status;
        this.reason = reason;
    }

    // Конструктор с Throwable
    public StatsValidationException(String message, String reason, Throwable cause) {
        super(message, cause);
        this.status = HttpStatus.BAD_REQUEST;
        this.reason = reason;
    }

    // Конструктор с 4 параметрами
    public StatsValidationException(String message, String reason, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.reason = reason;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }
}