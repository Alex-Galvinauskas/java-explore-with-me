package ru.practicum.stats.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventAdminRequest {
    @Schema(description = "Краткое описание")
    private String annotation;

    @Schema(description = "ID категории")
    private Long category;

    @Schema(description = "Полное описание")
    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Дата проведения")
    private String eventDate;

    @Schema(description = "Местоположение")
    private LocationDto location;

    @Schema(description = "Платное ли событие")
    private Boolean paid;

    @Schema(description = "Лимит участников")
    private Integer participantLimit;

    @Schema(description = "Нужна ли модерация запросов")
    private Boolean requestModeration;

    @Schema(description = "Действие со статусом")
    private String stateAction;

    @Schema(description = "Заголовок")
    private String title;
}