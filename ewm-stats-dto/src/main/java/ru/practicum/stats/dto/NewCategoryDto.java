package ru.practicum.stats.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для создания категории")
public class NewCategoryDto {

    @NotBlank
    @Size(min = 1, max = 50)
    @Schema(description = "Название категории", example = "Концерты")
    private String name;
}