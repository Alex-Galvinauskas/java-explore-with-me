package ru.practicum.stats.core.event.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.stats.dto.event.EventSearchParams;
import ru.practicum.stats.exception.NotFoundException;
import ru.practicum.stats.model.Event;
import ru.practicum.stats.model.enums.EventState;
import ru.practicum.stats.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventSearchService {

    private final EventRepository eventRepository;
    private final EventSearchParamsProcessor paramsProcessor;

    public List<Event> findPublishedEvents(EventSearchParams params, Pageable pageable) {
        EventSearchParams preparedParams = params;
        if (preparedParams.getRangeStart() == null || preparedParams.getRangeEnd() == null) {
            preparedParams = paramsProcessor.preparePublicSearchParams(params);
        }

        LocalDateTime rangeStart = preparedParams.getRangeStart();
        LocalDateTime rangeEnd = preparedParams.getRangeEnd();

        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(100);
        }

        return eventRepository.findPublishedEvents(
                EventState.PUBLISHED.name(),
                preparedParams.getText(),
                preparedParams.getCategories(),
                preparedParams.getPaid(),
                rangeStart,
                rangeEnd,
                preparedParams.getOnlyAvailable() != null ? preparedParams.getOnlyAvailable() : false,
                pageable
        );
    }

    public List<Event> findEventsByUser(Long userId, Pageable pageable) {
        return eventRepository.findByInitiatorId(userId, pageable);
    }

    public List<Event> findEventsByAdmin(EventSearchParams params, Pageable pageable) {
        EventSearchParams preparedParams = paramsProcessor.prepareAdminSearchParams(params);

        LocalDateTime rangeStart = preparedParams.getRangeStart();
        LocalDateTime rangeEnd = preparedParams.getRangeEnd();

        if (rangeStart == null) {
            rangeStart = LocalDateTime.now().minusYears(100);
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(100);
        }

        return eventRepository.findEventsByAdmin(
                preparedParams.getUsers(),
                preparedParams.getStates(),
                preparedParams.getCategories(),
                rangeStart,
                rangeEnd,
                pageable
        );
    }

    public Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));
    }

    public Event findPublishedEventById(Long eventId) {
        Event event = findEventById(eventId);
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Событие с id " + eventId + " не найдено");
        }
        return event;
    }
}