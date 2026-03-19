package ru.practicum.stats.service.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.dto.event.*;
import ru.practicum.stats.exception.NotFoundException;
import ru.practicum.stats.mapper.EventMapper;
import ru.practicum.stats.model.Category;
import ru.practicum.stats.model.Event;
import ru.practicum.stats.model.User;
import ru.practicum.stats.model.enums.EventState;
import ru.practicum.stats.repository.CategoryRepository;
import ru.practicum.stats.repository.EventRepository;
import ru.practicum.stats.service.event.builder.EventBuilder;
import ru.practicum.stats.service.event.builder.EventUpdater;
import ru.practicum.stats.service.event.enrichment.EventEnricher;
import ru.practicum.stats.service.event.enrichment.StatisticsService;
import ru.practicum.stats.service.event.search.EventSearchParamsProcessor;
import ru.practicum.stats.service.event.search.EventSearchService;
import ru.practicum.stats.service.event.validation.EventValidator;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;
    private final EventValidator validator;
    private final EventBuilder eventBuilder;
    private final EventUpdater eventUpdater;
    private final EventEnricher eventEnricher;
    private final StatisticsService statisticsService;
    private final EventSearchService searchService;
    private final EventSearchParamsProcessor paramsProcessor;

    @Override
    @Transactional
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        log.info("Добавление нового события пользователем с id: {}", userId);

        User initiator = validator.validateAndGetUser(userId);
        validator.validateCategoryExists(newEventDto.getCategory());

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория с id " +
                        newEventDto.getCategory() + " не найдена"));

        LocalDateTime eventDate =
                validator.parseAndValidateEventDate(newEventDto.getEventDate(), 2);

        Event event = eventBuilder.buildFromNewEventDto(newEventDto, initiator, category);
        event.setEventDate(eventDate);

        Event savedEvent = eventRepository.save(event);

        log.info("Событие успешно добавлено с id: {}", savedEvent.getId());
        return eventMapper.toFullDto(savedEvent);
    }

    @Override
    public List<EventShortDto> getEventsByUser(Long userId, int from, int size) {
        log.info("Получение событий пользователя с id: {}, from={}, size={}", userId, from, size);

        validator.validateUserExists(userId);
        validator.validatePagination(from, size);

        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = searchService.findEventsByUser(userId, pageable);

        eventEnricher.enrichEventsWithAdditionalData(events);

        log.info("Найдено {} событий для пользователя с id: {}", events.size(), userId);
        return mapToShortDtoList(events);
    }

    public EventFullDto getEvent(Long id, HttpServletRequest request) {
        Event event = findPublishedEventById(id);

        statisticsService.saveHit(request);

        Long views = statisticsService.getViewsForEvent(id);
        event.setViews(views);

        return eventMapper.toFullDto(event);
    }

    @Override
    public List<EventShortDto> getEvents(EventSearchParams params, HttpServletRequest request) {
        log.info("Поиск событий с параметрами: {}", params);

        paramsProcessor.validateSearchParams(params);

        Pageable pageable = paramsProcessor.createPageable(params);
        List<Event> events = searchService.findPublishedEvents(params, pageable);

        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        eventEnricher.enrichEventsWithAdditionalData(events);

        if (paramsProcessor.shouldSortByViews(params)) {
            sortEventsByViews(events);
        }

        statisticsService.saveHit(request);

        log.info("Найдено {} событий", events.size());
        return mapToShortDtoList(events);
    }

    @Override
    public EventFullDto getEventById(Long id) {
        Event event = findEventById(id);
        return eventMapper.toFullDto(event);
    }

    @Transactional
    @Override
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request) {
        log.info("Обновление события администратором, eventId={}, request={}", eventId, request);

        Event event = findEventById(eventId);

        eventUpdater.updateCategoryIfNeeded(event, request.getCategory());

        if (request.getEventDate() != null) {
            LocalDateTime newEventDate = validator.parseDate(request.getEventDate());
            validator.validateAdminEventDateUpdate(event, newEventDate);
            event.setEventDate(newEventDate);
        }

        EventState newState = eventUpdater.handleAdminStateAction(event, request.getStateAction());
        if (newState != null) {
            event.setState(newState);
        }

        eventUpdater.updateEventFields(event, request);

        Event updatedEvent = eventRepository.save(event);
        log.info("Событие обновлено администратором, eventId={}, новый статус={}",
                eventId, updatedEvent.getState());

        return eventMapper.toFullDto(updatedEvent);
    }

    @Override
    public List<EventFullDto> getEventsByAdmin(EventSearchParams params) {
        log.info("Поиск событий администратором с параметрами: {}", params);

        paramsProcessor.validateSearchParams(params);

        Pageable pageable = paramsProcessor.createPageable(params);
        List<Event> events = searchService.findEventsByAdmin(params, pageable);

        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        eventEnricher.enrichEventsWithAdditionalData(events);

        log.info("Найдено {} событий для администратора", events.size());
        return events.stream()
                .map(eventMapper::toFullDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEventByUser(Long userId, Long eventId) {
        log.info("Получение события пользователем, userId={}, eventId={}", userId, eventId);

        validator.validateUserExists(userId);

        Event event = findEventById(eventId);
        validator.validateUserIsInitiator(event, userId);

        eventEnricher.enrichEventsWithAdditionalData(List.of(event));

        return eventMapper.toFullDto(event);
    }

    @Transactional
    @Override
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest request) {
        log.info("Обновление события пользователем, userId={}, eventId={}, request={}",
                userId, eventId, request);

        validator.validateUserExists(userId);

        Event event = findEventById(eventId);

        validator.validateUserIsInitiator(event, userId);

        validator.validateEventNotPublished(event);

        eventUpdater.updateCategoryIfNeeded(event, request.getCategory());

        if (request.getEventDate() != null) {
            LocalDateTime newEventDate = validator.parseAndValidateEventDate(request.getEventDate(),
                    2);
            event.setEventDate(newEventDate);
        }

        EventState newState = eventUpdater.handleUserStateAction(request.getStateAction());
        if (newState != null) {
            event.setState(newState);
        }

        eventUpdater.updateEventFields(event, request);

        Event updatedEvent = eventRepository.save(event);
        log.info("Событие обновлено пользователем, eventId={}, новый статус={}",
                eventId, updatedEvent.getState());

        return eventMapper.toFullDto(updatedEvent);
    }

    private Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));
    }

    private Event findPublishedEventById(Long eventId) {
        Event event = findEventById(eventId);
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Событие с id " + eventId + " не найдено");
        }
        return event;
    }

    private List<EventShortDto> mapToShortDtoList(List<Event> events) {
        return events.stream()
                .map(eventMapper::toShortDto)
                .collect(Collectors.toList());
    }

    private void sortEventsByViews(List<Event> events) {
        Map<Long, Long> viewsMap = statisticsService.getViewsForEvents(
                events.stream().map(Event::getId).collect(Collectors.toList())
        );

        events.sort((e1, e2) -> {
            Long views1 = viewsMap.getOrDefault(e1.getId(), 0L);
            Long views2 = viewsMap.getOrDefault(e2.getId(), 0L);
            return views2.compareTo(views1);
        });
    }
}