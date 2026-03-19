package ru.practicum.main.dto.request;

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
    private List<Long> requestIds;

    @NotNull(message = "Status cannot be null")
    private String status;
}