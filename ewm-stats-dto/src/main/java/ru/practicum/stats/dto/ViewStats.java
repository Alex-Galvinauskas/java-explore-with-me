package ru.practicum.stats.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Статистика посещений")
public class ViewStats {

    @Schema(description = "Название сервиса", example = "ewm-main-service")
    private String app;

    @Schema(description = "URI", example = "/events/1")
    private String uri;

    @Schema(description = "Количество просмотров", example = "42")
    private Long hits;
}