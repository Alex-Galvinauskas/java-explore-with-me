package ru.practicum.stats.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequestDto {
    @Schema(description = "ID запроса")
    private Long id;

    @Schema(description = "Дата создания")
    private String created;

    @Schema(description = "ID события")
    private Long event;

    @Schema(description = "ID пользователя, отправившего запрос")
    private Long requester;

    @Schema(description = "Статус запроса")
    private String status;
}