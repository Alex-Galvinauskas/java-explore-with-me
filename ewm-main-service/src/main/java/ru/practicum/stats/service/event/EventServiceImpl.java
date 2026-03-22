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
import ru.practicum.stats.model.enums.RequestStatus;
import ru.practicum.stats.repository.CategoryRepository;
import ru.practicum.stats.repository.EventRepository;
import ru.practicum.stats.repository.RequestRepository;
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
    private final RequestRepository requestRepository;
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

        EventFullDto dto = eventMapper.toFullDto(savedEvent);
        dto.setConfirmedRequests(0L);
        dto.setViews(0L);

        return dto;
    }

    @Override
    public List<EventShortDto> getEventsByUser(Long userId, int from, int size) {
        log.info("Получение событий пользователя с id: {}, from={}, size={}", userId, from, size);

        validator.validateUserExists(userId);
        validator.validatePagination(from, size);

        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = searchService.findEventsByUser(userId, pageable);

        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Long> confirmedRequestsMap = getConfirmedRequestsCount(events);

        Map<Long, Long> viewsMap = statisticsService.getViewsForEvents(
                events.stream().map(Event::getId).collect(Collectors.toList())
        );

        return events.stream()
                .map(event -> {
                    EventShortDto dto = eventMapper.toShortDto(event);
                    dto.setConfirmedRequests(confirmedRequestsMap.getOrDefault(event.getId(), 0L));
                    dto.setViews(viewsMap.getOrDefault(event.getId(), 0L));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEvent(Long id, HttpServletRequest request) {
        Event event = findPublishedEventById(id);
        log.info("Getting event with id: {}", id);

        // Сохраняем хит асинхронно
        statisticsService.saveHit(request);

        // Получаем количество подтвержденных заявок
        Long confirmedRequests = requestRepository.countByEventIdAndStatus(id, RequestStatus.CONFIRMED);

        // Получаем количество просмотров
        Long views = statisticsService.getViewsForEvent(id);

        log.info("Event {}: confirmedRequests={}, views={}", id, confirmedRequests, views);

        EventFullDto dto = eventMapper.toFullDto(event);
        dto.setConfirmedRequests(confirmedRequests);
        dto.setViews(views);

        return dto;
    }

    @Override
    public List<EventShortDto> getEvents(EventSearchParams params, HttpServletRequest request) {
        log.info("Поиск событий с параметрами: {}", params);

        // Сохраняем хит асинхронно
        if (request != null) {
            statisticsService.saveHit(request);
        }

        // ВАЖНО: используем подготовленные параметры с дефолтными значениями
        EventSearchParams preparedParams = paramsProcessor.preparePublicSearchParams(params);

        paramsProcessor.validateSearchParams(preparedParams);

        Pageable pageable = paramsProcessor.createPageable(preparedParams);
        List<Event> events = searchService.findPublishedEvents(preparedParams, pageable);

        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Long> confirmedRequestsMap = getConfirmedRequestsCount(events);

        Map<Long, Long> viewsMap = statisticsService.getViewsForEvents(
                events.stream().map(Event::getId).collect(Collectors.toList())
        );

        List<EventShortDto> dtos = events.stream()
                .map(event -> {
                    EventShortDto dto = eventMapper.toShortDto(event);
                    dto.setConfirmedRequests(confirmedRequestsMap.getOrDefault(event.getId(), 0L));
                    dto.setViews(viewsMap.getOrDefault(event.getId(), 0L));
                    return dto;
                })
                .collect(Collectors.toList());

        // Фильтрация по onlyAvailable
        if (preparedParams.getOnlyAvailable() != null && preparedParams.getOnlyAvailable()) {
            dtos = dtos.stream()
                    .filter(this::isAvailable)
                    .collect(Collectors.toList());
        }

        // Сортировка по просмотрам если нужно
        if (paramsProcessor.shouldSortByViews(preparedParams)) {
            dtos.sort((d1, d2) -> Long.compare(d2.getViews(), d1.getViews()));
        }

        log.info("Найдено {} событий", dtos.size());
        return dtos;
    }

    @Override
    public EventFullDto getEventById(Long id) {
        Event event = findEventById(id);

        Long confirmedRequests = requestRepository.countByEventIdAndStatus(id, RequestStatus.CONFIRMED);

        EventFullDto dto = eventMapper.toFullDto(event);
        dto.setConfirmedRequests(confirmedRequests);
        dto.setViews(0L);

        return dto;
    }

    @Transactional
    @Override
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request) {
        log.info("Обновление события администратором, eventId={}, request={}", eventId, request);

        Event event = findEventById(eventId);

        eventUpdater.updateCategoryIfNeeded(event, request.getCategory());

        if (request.getEventDate() != null) {
            LocalDateTime newEventDate = validator.parseAndValidateEventDate(request.getEventDate(),
                    1);
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

        Long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        EventFullDto dto = eventMapper.toFullDto(updatedEvent);
        dto.setConfirmedRequests(confirmedRequests);
        dto.setViews(0L);

        return dto;
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

        Map<Long, Long> confirmedRequestsMap = getConfirmedRequestsCount(events);

        return events.stream()
                .map(event -> {
                    EventFullDto dto = eventMapper.toFullDto(event);
                    dto.setConfirmedRequests(confirmedRequestsMap.getOrDefault(event.getId(), 0L));
                    dto.setViews(0L);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEventByUser(Long userId, Long eventId) {
        log.info("Получение события пользователем, userId={}, eventId={}", userId, eventId);

        validator.validateUserExists(userId);

        Event event = findEventById(eventId);
        validator.validateUserIsInitiator(event, userId);

        Long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        EventFullDto dto = eventMapper.toFullDto(event);
        dto.setConfirmedRequests(confirmedRequests);
        dto.setViews(0L);

        return dto;
    }

    @Transactional
    @Override
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest request) {
        log.info("Обновление события пользователем, userId={}, eventId={}, request={}",
                userId, eventId, request);

        validator.validateUserExists(userId);

        Event event = findEventById(eventId);

        validator.validateUserIsInitiator(event, userId);

        LocalDateTime newEventDate = null;
        if (request.getEventDate() != null) {
            newEventDate = validator.parseAndValidateEventDate(request.getEventDate(), 2);
        }

        validator.validateEventNotPublished(event);

        eventUpdater.updateCategoryIfNeeded(event, request.getCategory());

        if (newEventDate != null) {
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

        Long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        EventFullDto dto = eventMapper.toFullDto(updatedEvent);
        dto.setConfirmedRequests(confirmedRequests);
        dto.setViews(0L);

        return dto;
    }

    /**
     * Получение количества подтвержденных заявок для списка событий
     */
    private Map<Long, Long> getConfirmedRequestsCount(List<Event> events) {
        if (events.isEmpty()) {
            return Collections.emptyMap();
        }

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

    /**
     * Проверка доступности события (не достигнут лимит участников)
     */
    private boolean isAvailable(EventShortDto event) {
        if (event.getParticipantLimit() == null || event.getParticipantLimit() == 0) {
            return true;
        }
        Long confirmedRequests = event.getConfirmedRequests();
        return confirmedRequests != null && confirmedRequests < event.getParticipantLimit();
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
}