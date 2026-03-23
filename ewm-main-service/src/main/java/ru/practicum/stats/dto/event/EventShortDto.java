package ru.practicum.stats.dto.event;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.stats.dto.category.CategoryDto;
import ru.practicum.stats.dto.user.UserShortDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventShortDto {
    @Schema(description = "ID события")
    private Long id;

    @Schema(description = "Краткое описание")
    private String annotation;

    @Schema(description = "Категория события")
    private CategoryDto category;

    @Schema(description = "Количество подтвержденных запросов")
    private Long confirmedRequests;

    @Schema(description = "Дата проведения")
    private String eventDate;

    @Schema(description = "Инициатор события")
    private UserShortDto initiator;

    @Schema(description = "Платное ли событие")
    private Boolean paid;

    @Schema(description = "Заголовок")
    private String title;

    @Schema(description = "Количество просмотров")
    private Long views;

    @Schema(description = "Лимит участников")
    private Integer participantLimit;
}