package ru.practicum.stats.core.event.search;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.practicum.stats.dto.event.EventShortDto;
import ru.practicum.stats.mapper.EventMapper;
import ru.practicum.stats.model.Event;
import ru.practicum.stats.dto.event.EventSearchParams;
import ru.practicum.stats.core.event.enrichment.EventResponseEnricher;
import ru.practicum.stats.statistics.event.StatisticsService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSearchOrchestrator {

    private final EventSearchParamsProcessor paramsProcessor;
    private final EventSearchService searchService;
    private final EventMapper eventMapper;
    private final EventResponseEnricher enricher;
    private final EventResponsePostProcessor postProcessor;
    private final StatisticsService statisticsService;

    public List<EventShortDto> searchPublic(EventSearchParams params, HttpServletRequest request) {
        log.info("Поиск событий с параметрами: {}", params);

        paramsProcessor.validateDateRange(params);

        if (request != null) {
            statisticsService.saveHit(request);
        }

        EventSearchParams preparedParams = paramsProcessor.preparePublicSearchParams(params);
        paramsProcessor.validateSearchParams(preparedParams);

        Pageable pageable = paramsProcessor.createPageable(preparedParams);
        List<Event> events = searchService.findPublishedEvents(preparedParams, pageable);

        if (events.isEmpty()) {
            return List.of();
        }

        List<EventShortDto> dtos = events.stream()
                .map(eventMapper::toShortDto)
                .collect(Collectors.toList());

        enricher.enrichShortDtos(dtos, events);

        dtos = postProcessor.filterByAvailability(dtos, preparedParams.getOnlyAvailable());
        dtos = postProcessor.sortByViewsIfNeeded(dtos, preparedParams);

        log.info("Найдено {} событий", dtos.size());
        return dtos;
    }

    public List<EventShortDto> searchByUser(List<Event> events) {
        if (events.isEmpty()) {
            return List.of();
        }

        List<EventShortDto> dtos = events.stream()
                .map(eventMapper::toShortDto)
                .collect(Collectors.toList());

        enricher.enrichShortDtos(dtos, events);
        return dtos;
    }
}