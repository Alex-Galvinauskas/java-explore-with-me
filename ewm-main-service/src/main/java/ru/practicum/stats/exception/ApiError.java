package ru.practicum.stats.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ApiError {

    private List<String> errors;
    private String message;
    private String reason;
    private String status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    public static ApiError of(HttpStatus status, String reason, String message) {
        return ApiError.builder()
                .status(status.name())
                .reason(reason)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ApiError of(HttpStatus status, String reason, String message, List<String> errors) {
        return ApiError.builder()
                .status(status.name())
                .reason(reason)
                .message(message)
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();
    }
}