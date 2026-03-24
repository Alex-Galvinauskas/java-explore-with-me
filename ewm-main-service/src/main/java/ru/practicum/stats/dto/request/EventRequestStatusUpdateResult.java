package ru.practicum.stats.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateResult {
    @Schema(description = "Подтвержденные запросы")
    private List<ParticipationRequestDto> confirmedRequests;

    @Schema(description = "Отклоненные запросы")
    private List<ParticipationRequestDto> rejectedRequests;
}