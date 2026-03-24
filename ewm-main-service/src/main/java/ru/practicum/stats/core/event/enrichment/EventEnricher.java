package ru.practicum.stats.core.event.enrichment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
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
public class EventEnricher {

    private final RequestRepository requestRepository;
    private final StatisticsService statisticsService;
    private static final long DEFAULT_CONFIRMED_REQUESTS = 0L;

    public void enrichEventsWithAdditionalData(List<Event> events) {
        if (events.isEmpty()) return;

        updateConfirmedRequests(events);
        updateViews(events);
    }

    private void updateConfirmedRequests(List<Event> events) {
        List<Long> eventIds = extractEventIds(events);
        Map<Long, Long> confirmedMap = getConfirmedRequestsMap(eventIds);

        events.forEach(event ->
                event.setConfirmedRequests(confirmedMap.getOrDefault(event.getId(),
                        DEFAULT_CONFIRMED_REQUESTS))
        );
    }

    private void updateViews(List<Event> events) {
        List<Long> eventIds = extractEventIds(events);
        Map<Long, Long> viewsMap = statisticsService.getViewsForEvents(eventIds);

        events.forEach(event ->
                event.setViews(viewsMap.getOrDefault(event.getId(), 0L))
        );
    }

    private List<Long> extractEventIds(List<Event> events) {
        return events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());
    }

    private Map<Long, Long> getConfirmedRequestsMap(List<Long> eventIds) {
        List<Object[]> counts = requestRepository.countByEventIdsAndStatus(eventIds, RequestStatus.CONFIRMED);

        return counts.stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> (Long) arr[1]
                ));
    }
}