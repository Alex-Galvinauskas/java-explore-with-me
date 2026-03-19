package ru.practicum.main.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Schema(description = "Информация об ошибке")
public class ApiError {

    @Schema(description = "Статус ошибки", example = "BAD_REQUEST")
    private HttpStatus status;

    @Schema(description = "Причина ошибки", example = "Ошибка валидации")
    private String reason;

    @Schema(description = "Сообщение об ошибке", example = "Переданы некорректные данные")
    private String message;

    @Schema(description = "Список детальных ошибок")
    private List<String> errors;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Время возникновения ошибки", example = "2022-09-06 12:00:00")
    private LocalDateTime timestamp;

    public static ApiError of(HttpStatus status, String reason, String message) {
        return ApiError.builder()
                .status(status)
                .reason(reason)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ApiError of(HttpStatus status, String reason, String message, List<String> errors) {
        return ApiError.builder()
                .status(status)
                .reason(reason)
                .message(message)
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();
    }
}