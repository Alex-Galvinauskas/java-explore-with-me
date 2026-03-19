package ru.practicum.stats.exception;

import org.springframework.http.HttpStatus;

/**
 * Исключение для ошибок валидации (400 Bad Request)
 */
public class BadRequestException extends StatsValidationException {

    public BadRequestException(String message) {
        super(message, "Некорректный запрос", HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(String message, String reason) {
        super(message, reason, HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, "Некорректный запрос", HttpStatus.BAD_REQUEST, cause);
    }
}