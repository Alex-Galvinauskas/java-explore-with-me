package ru.practicum.stats.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventUserRequest {

    @Size(min = 20, max = 2000, message = "Аннотация должна быть от 20 до 2000 символов")
    @Schema(description = "Краткое описание")
    private String annotation;

    @Schema(description = "ID категории")
    private Long category;

    @Size(min = 20, max = 7000, message = "Описание должно быть от 20 до 7000 символов")
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

    @Size(min = 3, max = 120, message = "Длина заголовка должна быть от 3 до 120 символов")
    @Schema(description = "Заголовок")
    private String title;
}