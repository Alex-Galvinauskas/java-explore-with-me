package ru.practicum.main.dto.event;

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
    @NotNull(message = "Latitude cannot be null")
    private Float lat;

    @NotNull(message = "Longitude cannot be null")
    private Float lon;
}