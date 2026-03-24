package ru.practicum.stats.dto.event;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationDto {
    @NotNull(message = "Широта не может быть пустой")
    @Schema(description = "Широта")
    private Float lat;

    @NotNull(message = "Долгота не может быть пустой")
    @Schema(description = "Долгота")
    private Float lon;
}