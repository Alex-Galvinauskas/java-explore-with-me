package ru.practicum.stats.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Данные о запросе к эндпоинту")
public class EndpointHit {

    @Schema(description = "Идентификатор записи", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Идентификатор сервиса", example = "ewm-main-service")
    private String app;

    @NotBlank
    @Size(max = 512)
    @Schema(description = "URI запроса", example = "/events/1")
    private String uri;

    @NotBlank
    @Pattern(regexp = "^(([0-9]{1,3}\\.){3}[0-9]{1,3}|([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|::1|localhost)$")
    @Size(max = 45)
    @Schema(description = "IP-адрес пользователя", example = "192.168.1.1")
    private String ip;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @PastOrPresent
    @Schema(description = "Дата и время запроса", example = "2024-03-24 15:30:45")
    private LocalDateTime timestamp;
}