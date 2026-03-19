package ru.practicum.main.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Глобальный обработчик ошибок для REST API
 */
@Slf4j
@RestControllerAdvice
public class ErrorHandler {


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        log.error("Ошибка валидации: {}", ex.getMessage());

        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.toList());

        ApiError apiError = ApiError.of(
                HttpStatus.BAD_REQUEST,
                "Ошибка валидации",
                "Переданы некорректные данные",
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex) {
        log.error("Ошибка валидации параметров: {}", ex.getMessage());

        List<String> errors = ex.getConstraintViolations().stream()
                .map(violation ->
                        violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.toList());

        ApiError apiError = ApiError.of(
                HttpStatus.BAD_REQUEST,
                "Ошибка валидации параметров",
                "Переданы некорректные параметры",
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParams(MissingServletRequestParameterException ex) {
        log.error("Отсутствует обязательный параметр: {}", ex.getMessage());

        ApiError apiError = ApiError.of(
                HttpStatus.BAD_REQUEST,
                "Отсутствует обязательный параметр",
                String.format("Параметр '%s' типа '%s' обязателен",
                        ex.getParameterName(), ex.getParameterType())
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ApiError> handleDateTimeParse(DateTimeParseException ex) {
        log.error("Ошибка парсинга даты: {}", ex.getMessage());

        ApiError apiError = ApiError.of(
                HttpStatus.BAD_REQUEST,
                "Ошибка формата даты",
                "Дата должна быть в формате yyyy-MM-dd HH:mm:ss"
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.error("Ошибка чтения HTTP сообщения: {}", ex.getMessage());

        ApiError apiError = ApiError.of(
                HttpStatus.BAD_REQUEST,
                "Ошибка формата запроса",
                "Тело запроса содержит некорректные данные"
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.error("Ошибка типа аргумента: {}", ex.getMessage());

        String message = String.format("Параметр '%s' должен быть типа %s",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "неизвестный тип");

        ApiError apiError = ApiError.of(
                HttpStatus.BAD_REQUEST,
                "Ошибка типа данных",
                message
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex) {
        log.error("Bad request: {}", ex.getMessage());

        ApiError apiError = ApiError.of(
                ex.getStatus(),
                ex.getReason(),
                ex.getMessage()
        );

        return ResponseEntity.status(ex.getStatus()).body(apiError);
    }

    @ExceptionHandler(StatsValidationException.class)
    public ResponseEntity<ApiError> handleStatsValidation(StatsValidationException ex) {
        log.error("Ошибка валидации статистики: {}", ex.getMessage());

        ApiError apiError = ApiError.of(
                ex.getStatus(),
                ex.getReason(),
                ex.getMessage()
        );

        return ResponseEntity.status(ex.getStatus()).body(apiError);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Illegal argument: {}", ex.getMessage());

        ApiError apiError = ApiError.of(
                HttpStatus.BAD_REQUEST,
                "Некорректный аргумент",
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("Нарушение целостности данных: {}", ex.getMessage());

        ApiError apiError = ApiError.of(
                HttpStatus.CONFLICT,
                "Нарушение целостности данных",
                "Операция нарушает целостность данных"
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAllExceptions(Exception ex) {
        log.error("Внутренняя ошибка сервера: ", ex);

        ApiError apiError = ApiError.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Внутренняя ошибка сервера",
                "Произошла непредвиденная ошибка"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }

    private String formatFieldError(FieldError error) {
        String field = error.getField();
        String message = error.getDefaultMessage();
        return String.format("%s: %s", field, message);
    }
}