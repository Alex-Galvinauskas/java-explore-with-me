package ru.practicum.stats.service.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.core.event.builder.EventBuilder;
import ru.practicum.stats.core.event.builder.EventUpdater;
import ru.practicum.stats.core.event.enrichment.EventResponseEnricher;
import ru.practicum.stats.core.event.search.EventSearchOrchestrator;
import ru.practicum.stats.core.event.search.EventSearchParamsProcessor;
import ru.practicum.stats.core.event.search.EventSearchService;
import ru.practicum.stats.dto.event.*;
import ru.practicum.stats.exception.NotFoundException;
import ru.practicum.stats.mapper.EventMapper;
import ru.practicum.stats.model.Category;
import ru.practicum.stats.model.Event;
import ru.practicum.stats.model.User;
import ru.practicum.stats.model.enums.EventState;
import ru.practicum.stats.repository.CategoryRepository;
import ru.practicum.stats.repository.EventRepository;
import ru.practicum.stats.statistics.event.StatisticsService;
import ru.practicum.stats.validation.event.EventValidator;

import java.time.LocalDateTime;
import java.util.List;
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
    private final EventResponseEnricher enricher;
    private final EventSearchOrchestrator searchOrchestrator;
    private final EventSearchService searchService;
    private final EventSearchParamsProcessor paramsProcessor;
    private final StatisticsService statisticsService;

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
        enricher.enrichFullDtoWithViews(dto, savedEvent);

        return dto;
    }

    @Override
    public List<EventShortDto> getEventsByUser(Long userId, int from, int size) {
        log.info("Получение событий пользователя с id: {}, from={}, size={}", userId, from, size);

        validator.validateUserExists(userId);
        validator.validatePagination(from, size);

        Pageable pageable = paramsProcessor.createPageable(EventSearchParams.builder()
                .from(from)
                .size(size)
                .build());

        List<Event> events = searchService.findEventsByUser(userId, pageable);

        return searchOrchestrator.searchByUser(events);
    }

    @Override
    public EventFullDto getEvent(Long id, HttpServletRequest request) {
        Event event = findPublishedEventById(id);
        log.info("Получение события с id: {}", id);

        statisticsService.saveHit(request);

        EventFullDto dto = eventMapper.toFullDto(event);
        enricher.enrichFullDtoWithViews(dto, event);

        log.info("Событие {}: просмотров={}", id, dto.getViews());
        return dto;
    }

    @Override
    public List<EventShortDto> getEvents(EventSearchParams params, HttpServletRequest request) {
        return searchOrchestrator.searchPublic(params, request);
    }

    @Override
    public EventFullDto getEventById(Long id) {
        Event event = findEventById(id);

        EventFullDto dto = eventMapper.toFullDto(event);
        enricher.enrichFullDto(dto, event);
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
            LocalDateTime newEventDate = validator.parseAndValidateEventDateForAdmin(request.getEventDate(),
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

        EventFullDto dto = eventMapper.toFullDto(updatedEvent);
        enricher.enrichFullDto(dto, updatedEvent);
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
            return List.of();
        }

        return events.stream()
                .map(event -> {
                    EventFullDto dto = eventMapper.toFullDto(event);
                    enricher.enrichFullDto(dto, event);
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

        EventFullDto dto = eventMapper.toFullDto(event);
        enricher.enrichFullDto(dto, event);
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

        if (request.getEventDate() != null) {
            LocalDateTime newEventDate = validator.parseAndValidateEventDate(request.getEventDate(),
                    2);
            event.setEventDate(newEventDate);
        }

        validator.validateEventNotPublished(event);

        eventUpdater.updateCategoryIfNeeded(event, request.getCategory());

        EventState newState = eventUpdater.handleUserStateAction(request.getStateAction());
        if (newState != null) {
            event.setState(newState);
        }

        eventUpdater.updateEventFields(event, request);

        Event updatedEvent = eventRepository.save(event);
        log.info("Событие обновлено пользователем, eventId={}, новый статус={}",
                eventId, updatedEvent.getState());

        EventFullDto dto = eventMapper.toFullDto(updatedEvent);
        enricher.enrichFullDto(dto, updatedEvent);
        dto.setViews(0L);

        return dto;
    }

    @Transactional
    @Override
    public void deleteEventByAdmin(Long eventId) {
        log.info("Удаление события администратором, eventId={}", eventId);

        Event event = findEventById(eventId);

        eventRepository.delete(event);

        log.info("Событие успешно удалено, eventId={}", eventId);
    }

    private Event findEventById(Long eventId) {
        Event event = eventRepository.findByIdWithDetails(eventId);
        if (event == null) {
            throw new NotFoundException("Событие с id " + eventId + " не найдено");
        }
        return event;
    }

    private Event findPublishedEventById(Long eventId) {
        Event event = findEventById(eventId);
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Событие с id " + eventId + " не найдено");
        }
        return event;
    }
}