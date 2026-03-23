package ru.practicum.stats.core.event.enrichment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.stats.dto.event.EventFullDto;
import ru.practicum.stats.dto.event.EventShortDto;
import ru.practicum.stats.model.Event;
import ru.practicum.stats.model.enums.RequestStatus;
import ru.practicum.stats.repository.RequestRepository;
import ru.practicum.stats.statistics.event.StatisticsService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventResponseEnricher {

    private final RequestRepository requestRepository;
    private final StatisticsService statisticsService;

    public void enrichShortDtos(List<EventShortDto> dtos, List<Event> events) {
        if (events.isEmpty() || dtos.isEmpty()) {
            return;
        }

        Map<Long, Long> confirmedRequestsMap = getConfirmedRequestsMap(events);
        Map<Long, Long> viewsMap = statisticsService.getViewsForEvents(
                events.stream().map(Event::getId).collect(Collectors.toList())
        );

        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            EventShortDto dto = dtos.get(i);

            dto.setConfirmedRequests(confirmedRequestsMap.getOrDefault(event.getId(), 0L));
            dto.setViews(viewsMap.getOrDefault(event.getId(), 0L));
        }
    }

    public void enrichFullDto(EventFullDto dto, Event event) {
        Long confirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(),
                RequestStatus.CONFIRMED);
        dto.setConfirmedRequests(confirmedRequests);
    }

    public void enrichFullDtoWithViews(EventFullDto dto, Event event) {
        enrichFullDto(dto, event);
        Long views = statisticsService.getViewsForEvent(event.getId());
        dto.setViews(views);
    }

    private Map<Long, Long> getConfirmedRequestsMap(List<Event> events) {
        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        List<Object[]> results = requestRepository.countByEventIdsAndStatus(eventIds, RequestStatus.CONFIRMED);

        return results.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }
}