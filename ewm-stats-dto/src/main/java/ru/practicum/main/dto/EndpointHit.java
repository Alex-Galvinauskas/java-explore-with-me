package ru.practicum.main.dto;

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

    @NotBlank(message = "Идентификатор сервиса не может быть пустым")
    @Size(max = 255, message = "Длина идентификатора сервиса не может превышать 255 символов")
    @Schema(description = "Идентификатор сервиса", example = "ewm-main-service", requiredMode = Schema.RequiredMode.REQUIRED)
    private String app;

    @NotBlank(message = "URI не может быть пустым")
    @Size(max = 512, message = "Длина URI не может превышать 512 символов")
    @Schema(description = "URI запроса", example = "/events/1", requiredMode = Schema.RequiredMode.REQUIRED)
    private String uri;

    @NotBlank(message = "IP-адрес не может быть пустым")
    @Pattern(regexp = "^(([0-9]{1,3}\\.){3}[0-9]{1,3}|([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|::1|localhost)$",
            message = "Некорректный формат IP-адреса")
    @Size(max = 45, message = "Длина IP-адреса не может превышать 45 символов")
    @Schema(description = "IP-адрес пользователя", example = "192.168.1.1", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ip;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @PastOrPresent(message = "Время запроса не может быть в будущем")
    @Schema(description = "Дата и время запроса", example = "2022-09-06 10:00:00",
            pattern = "yyyy-MM-dd HH:mm:ss", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime timestamp;
}