package ru.practicum.stats.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice("ru.practicum.stats.controller")
public class ErrorHandler {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final NotFoundException e) {
        log.error("Объект не найден: {}", e.getMessage());
        return ApiError.builder()
                .status(HttpStatus.NOT_FOUND.name())
                .reason("Запрашиваемый объект не найден.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .errors(List.of())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(final ConflictException e) {
        log.error("Конфликт данных: {}", e.getMessage());
        return ApiError.builder()
                .status(HttpStatus.CONFLICT.name())
                .reason("Условия для выполнения запроса не соблюдены.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .errors(List.of())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolationException(final DataIntegrityViolationException e) {
        log.error("Нарушение целостности данных: {}", e.getMessage());

        String message = e.getMessage();
        String reason = "Нарушено ограничение целостности.";
        String userMessage = message;

        if (message != null) {
            userMessage = switch (message) {
                case String msg when msg.contains("uq_category_name") -> "Категория с таким именем уже существует";
                case String msg when msg.contains("uq_email") -> "Пользователь с таким email уже существует";
                case String msg when msg.contains("uq_request") -> "Заявка уже существует";
                case String msg when msg.contains("uq_compilation_name") -> "Подборка с таким названием уже существует";
                default -> message;
            };
        }

        return ApiError.builder()
                .status(HttpStatus.CONFLICT.name())
                .reason(reason)
                .message(userMessage)
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .errors(List.of(e.getMessage()))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        log.error("Ошибка валидации: {}", e.getMessage());

        String errors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("Поле: %s. Ошибка: %s. Значение: %s",
                        error.getField(), error.getDefaultMessage(), error.getRejectedValue()))
                .collect(Collectors.joining("; "));

        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Некорректно составленный запрос.")
                .message(errors)
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .errors(List.of())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleConstraintViolationException(final ConstraintViolationException e) {
        log.error("Ошибка валидации параметров: {}", e.getMessage());

        String errors = e.getConstraintViolations().stream()
                .map(violation -> String.format("Поле: %s. Ошибка: %s. Значение: %s",
                        violation.getPropertyPath(),
                        violation.getMessage(),
                        violation.getInvalidValue()))
                .collect(Collectors.joining("; "));

        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Некорректно составленный запрос.")
                .message(errors)
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .errors(List.of())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingServletRequestParameterException(
            final MissingServletRequestParameterException e) {
        log.error("Отсутствует обязательный параметр запроса: {}", e.getMessage());

        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Некорректно составленный запрос.")
                .message(String.format("Отсутствует обязательный параметр '%s'", e.getParameterName()))
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .errors(List.of(e.getClass().getName()))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleHttpMessageNotReadableException(
            final HttpMessageNotReadableException e) {
        log.error("Некорректный формат JSON: {}", e.getMessage());

        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Некорректно составленный запрос.")
                .message("Некорректный JSON запрос")
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .errors(List.of(e.getClass().getName()))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(final ValidationException e) {
        log.error("Ошибка валидации: {}", e.getMessage());

        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Некорректно составленный запрос.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .errors(List.of(e.getClass().getName()))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleForbiddenException(final ForbiddenException e) {
        log.error("Доступ запрещен: {}", e.getMessage());

        return ApiError.builder()
                .status(HttpStatus.FORBIDDEN.name())
                .reason("Условия для выполнения запроса не соблюдены.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .errors(List.of())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalArgumentException(final IllegalArgumentException e) {
        log.error("Некорректный параметр запроса: {}", e.getMessage());

        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Некорректно составленный запрос.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .errors(List.of())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleThrowable(final Throwable e) {
        log.error("Непредвиденная ошибка: {}", e.getMessage(), e);
        return ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                .reason("Внутренняя ошибка сервера")
                .message("Произошла непредвиденная ошибка")
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .errors(List.of(e.getClass().getName()))
                .build();
    }
}