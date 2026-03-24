package ru.practicum.stats.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {
    @NotBlank(message = "Аннотация не может быть пустой")
    @Size(min = 20, max = 2000, message = "Аннотация должна быть от 20 до 2000 символов")
    @Schema(description = "Краткое описание")
    private String annotation;

    @NotNull(message = "Категория не может быть пустой")
    @Schema(description = "ID категории")
    private Long category;

    @NotBlank(message = "Описание не может быть пустым")
    @Size(min = 20, max = 7000, message = "Описание должно быть от 20 до 7000 символов")
    @Schema(description = "Полное описание")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Дата проведения")
    private String eventDate;

    @NotNull(message = "Локация не может быть пустой")
    @Schema(description = "Местоположение")
    private LocationDto location;

    @Schema(description = "Платное ли событие")
    private Boolean paid;

    @PositiveOrZero(message = "Лимит участников не может быть отрицательным")
    @Schema(description = "Лимит участников")
    private Integer participantLimit;

    @Schema(description = "Нужна ли модерация запросов")
    private Boolean requestModeration;

    @NotBlank(message = "Заголовок не может быть пустым")
    @Size(min = 3, max = 120, message = "Заголовок должен быть от 3 до 120 символов")
    @Schema(description = "Заголовок")
    private String title;
}