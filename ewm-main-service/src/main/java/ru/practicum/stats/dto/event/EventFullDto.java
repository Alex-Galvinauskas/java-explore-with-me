package ru.practicum.stats.dto.event;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.stats.dto.category.CategoryDto;
import ru.practicum.stats.dto.user.UserShortDto;
import ru.practicum.stats.model.Comment;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventFullDto {
    @Schema(description = "ID события")
    private Long id;

    @Schema(description = "Краткое описание")
    private String annotation;

    @Schema(description = "Категория события")
    private CategoryDto category;

    @Schema(description = "Количество подтвержденных запросов")
    private Long confirmedRequests;

    @Schema(description = "Дата создания события")
    private String createdOn;

    @Schema(description = "Полное описание")
    private String description;

    @Schema(description = "Дата проведения")
    private String eventDate;

    @Schema(description = "Инициатор события")
    private UserShortDto initiator;

    @Schema(description = "Местоположение")
    private LocationDto location;

    @Schema(description = "Платное ли событие")
    private Boolean paid;

    @Schema(description = "Лимит участников")
    private Integer participantLimit;

    @Schema(description = "Дата публикации")
    private String publishedOn;

    @Schema(description = "Нужна ли модерация запросов")
    private Boolean requestModeration;

    @Schema(description = "Статус события")
    private String state;

    @Schema(description = "Заголовок")
    private String title;

    @Schema(description = "Количество просмотров")
    private Long views;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();
}