package ru.practicum.stats.core.event.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.stats.dto.event.EventShortDto;
import ru.practicum.stats.dto.event.EventSearchParams;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventResponsePostProcessor {

    public List<EventShortDto> filterByAvailability(List<EventShortDto> dtos, Boolean onlyAvailable) {
        if (onlyAvailable == null || !onlyAvailable) {
            return dtos;
        }

        return dtos.stream()
                .filter(this::isAvailable)
                .collect(Collectors.toList());
    }

    public List<EventShortDto> sortByViewsIfNeeded(List<EventShortDto> dtos, EventSearchParams params) {
        if (shouldSortByViews(params)) {
            dtos.sort((d1, d2) -> Long.compare(d2.getViews(), d1.getViews()));
        }
        return dtos;
    }

    private boolean shouldSortByViews(EventSearchParams params) {
        return "VIEWS".equalsIgnoreCase(params.getSort());
    }

    private boolean isAvailable(EventShortDto event) {
        if (event.getParticipantLimit() == null || event.getParticipantLimit() == 0) {
            return true;
        }
        Long confirmedRequests = event.getConfirmedRequests();
        return confirmedRequests != null && confirmedRequests < event.getParticipantLimit();
    }
}