package ru.practicum.stats.dto.compilation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.stats.dto.event.EventShortDto;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompilationDto {
    @Schema(description = "ID подборки")
    private Long id;

    @Schema(description = "Список событий в подборке")
    private List<EventShortDto> events;

    @Schema(description = "Закреплена ли подборка на главной странице")
    private Boolean pinned;

    @Schema(description = "Заголовок подборки")
    private String title;
}