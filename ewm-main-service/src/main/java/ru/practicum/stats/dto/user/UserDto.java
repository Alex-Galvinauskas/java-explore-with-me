package ru.practicum.stats.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    @Schema(description = "ID пользователя")
    private Long id;

    @Schema(description = "Имя пользователя")
    private String name;

    @Schema(description = "Email пользователя")
    private String email;
}