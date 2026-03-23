package ru.practicum.stats.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateRequest {
    @NotNull(message = "Request ids cannot be null")
    @Schema(description = "Список ID запросов")
    private List<Long> requestIds;

    @NotNull(message = "Status cannot be null")
    @Schema(description = "Новый статус")
    private String status;
}