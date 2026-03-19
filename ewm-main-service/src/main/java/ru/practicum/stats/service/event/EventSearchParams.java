package ru.practicum.stats.service.event;

import lombok.Builder;
import lombok.Data;
import ru.practicum.stats.model.enums.EventState;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class EventSearchParams {
    private String text;
    private List<Long> categories;
    private Boolean paid;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private Boolean onlyAvailable;
    private String sort;
    private Integer from;
    private Integer size;

    private Long userId;

    private List<Long> users;
    private List<EventState> states;
}