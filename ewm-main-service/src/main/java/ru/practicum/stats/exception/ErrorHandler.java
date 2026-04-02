package ru.practicum.stats.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Hidden
@RestControllerAdvice
@RequiredArgsConstructor
public class ErrorHandler {

    private final ObjectMapper objectMapper;

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParams(MissingServletRequestParameterException ex) {
        log.error("Отсутствует обязательный параметр: {}", ex.getMessage());

        ApiError apiError = ApiError.of(
                HttpStatus.BAD_REQUEST,
                "Некорректно составленный запрос",
                String.format("Отсутствует обязательный параметр '%s'", ex.getParameterName())
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Некорректный аргумент: {}", ex.getMessage());

        ApiError apiError = ApiError.of(
                HttpStatus.BAD_REQUEST,
                "Некорректный аргумент",
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiError> handleValidation(ValidationException ex) {
        log.error("Ошибка валидации: {}", ex.getMessage());

        ApiError apiError = ApiError.of(
                HttpStatus.BAD_REQUEST,
                "Ошибка валидации",
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex) {
        log.error("Bad request: {}", ex.getMessage());

        ApiError apiError = ApiError.of(
                HttpStatus.BAD_REQUEST,
                "Некорректный запрос",
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiError> handleForbidden(ForbiddenException ex) {
        log.error("Доступ запрещен: {}", ex.getMessage());

        ApiError apiError = ApiError.of(
                HttpStatus.FORBIDDEN,
                "Доступ запрещен",
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiError);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex) {
        log.error("Объект не найден: {}", ex.getMessage());

        ApiError apiError = ApiError.of(
                HttpStatus.NOT_FOUND,
                "Объект не найден",
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<ApiError> handleCommentNotFound(CommentNotFoundException ex) {
        log.error("Comment not found: {}", ex.getMessage());

        ApiError apiError = ApiError.of(
                HttpStatus.NOT_FOUND,
                "Комментарий не найден",
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("Нарушение целостности данных: {}", ex.getMessage());

        String detailedMessage = ex.getMostSpecificCause().getMessage();

        if (detailedMessage != null &&
                (detailedMessage.contains("value too long") ||
                        detailedMessage.contains("max length") ||
                        detailedMessage.contains("exceeds the maximum length") ||
                        detailedMessage.contains("too long"))) {

            String fieldName = extractFieldName(detailedMessage);
            String friendlyMessage = fieldName != null
                    ? "Поле '" + fieldName + "' превышает допустимую длину"
                    : "Значение поля превышает допустимую длину";

            ApiError apiError = ApiError.of(
                    HttpStatus.BAD_REQUEST,
                    "Ошибка валидации",
                    friendlyMessage,
                    List.of(detailedMessage)
            );

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
        }

        String userMessage = extractUserMessage(ex.getMessage());

        ApiError apiError = ApiError.of(
                HttpStatus.CONFLICT,
                "Нарушение целостности данных",
                userMessage,
                List.of(ex.getMessage())
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex) {
        log.error("Конфликт: {}", ex.getMessage());

        ApiError apiError = ApiError.of(
                HttpStatus.CONFLICT,
                "Конфликт данных",
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ApiError> handleHttpClientError(HttpClientErrorException ex) {
        log.error("HttpClientErrorException: status={}, body={}", ex.getStatusCode(), ex.getResponseBodyAsString());

        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

        ApiError apiError;
        try {
            apiError = objectMapper.readValue(ex.getResponseBodyAsString(), ApiError.class);
        } catch (Exception e) {
            apiError = ApiError.of(
                    status,
                    "Ошибка сервиса статистики",
                    ex.getResponseBodyAsString()
            );
        }

        return ResponseEntity.status(status).body(apiError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAllExceptions(Exception ex) {
        log.error("Внутренняя ошибка сервера: ", ex);
        log.error("Тип исключения: {}", ex.getClass().getName());

        ApiError apiError = ApiError.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Внутренняя ошибка сервера",
                "Произошла непредвиденная ошибка"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }

    private String formatFieldError(FieldError error) {
        return String.format("%s: %s (значение: %s)",
                error.getField(),
                error.getDefaultMessage(),
                error.getRejectedValue());
    }

    private String extractFieldName(String message) {
        if (message == null) return null;

        int startIdx = message.indexOf("column \"");
        if (startIdx != -1) {
            startIdx += 8;
            int endIdx = message.indexOf("\"", startIdx);
            if (endIdx != -1) {
                return message.substring(startIdx, endIdx);
            }
        }

        return null;
    }

    private String extractUserMessage(String message) {
        if (message == null) {
            return "Операция нарушает целостность данных";
        }

        if (message.contains("uq_category_name")) {
            return "Категория с таким именем уже существует";
        }
        if (message.contains("uq_email")) {
            return "Пользователь с таким email уже существует";
        }
        if (message.contains("uq_request")) {
            return "Заявка уже существует";
        }
        if (message.contains("uq_compilation_name")) {
            return "Подборка с таким названием уже существует";
        }
        if (message.contains("value too long") ||
                message.contains("max length") ||
                message.contains("exceeds the maximum length") ||
                message.contains("too long")) {
            return "Значение поля превышает максимально допустимую длину";
        }

        return message;
    }
}