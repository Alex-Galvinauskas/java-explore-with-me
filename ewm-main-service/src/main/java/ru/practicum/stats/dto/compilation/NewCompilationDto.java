package ru.practicum.stats.dto.compilation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewCompilationDto {
    @Schema(description = "Список ID событий в подборке")
    private List<Long> events;

    @Schema(description = "Закрепить подборку на главной странице")
    private Boolean pinned;

    @NotBlank(message = "Заголовок не может быть пустым")
    @Size(max = 50, message = "Заголовок должен быть от 1 до 50 символов")
    @Schema(description = "Заголовок подборки")
    private String title;
}