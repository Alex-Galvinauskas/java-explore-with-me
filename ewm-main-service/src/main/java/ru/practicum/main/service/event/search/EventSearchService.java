package ru.practicum.main.service.event.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.enums.EventState;
import ru.practicum.main.repository.EventRepository;
import ru.practicum.main.service.event.EventSearchParams;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventSearchService {

    private final EventRepository eventRepository;
    private final EventSearchParamsProcessor paramsProcessor;

    public List<Event> findPublishedEvents(EventSearchParams params, Pageable pageable) {
        EventSearchParams preparedParams = paramsProcessor.preparePublicSearchParams(params);

        return eventRepository.findPublishedEvents(
                EventState.PUBLISHED,
                preparedParams.getText(),
                preparedParams.getCategories(),
                preparedParams.getPaid(),
                preparedParams.getRangeStart(),
                preparedParams.getRangeEnd(),
                preparedParams.getOnlyAvailable() != null ? preparedParams.getOnlyAvailable() : false,
                pageable
        );
    }

    public List<Event> findEventsByUser(Long userId, Pageable pageable) {
        return eventRepository.findByInitiatorId(userId, pageable);
    }

    public List<Event> findEventsByAdmin(EventSearchParams params, Pageable pageable) {
        EventSearchParams preparedParams = paramsProcessor.prepareAdminSearchParams(params);

        return eventRepository.findEventsByAdmin(
                preparedParams.getUsers(),
                preparedParams.getStates(),
                preparedParams.getCategories(),
                preparedParams.getRangeStart(),
                preparedParams.getRangeEnd(),
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