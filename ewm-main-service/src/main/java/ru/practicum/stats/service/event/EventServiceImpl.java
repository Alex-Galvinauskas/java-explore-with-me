package ru.practicum.stats.service.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.stats.dto.event.EventFullDto;
import ru.practicum.stats.dto.event.EventShortDto;
import ru.practicum.stats.dto.event.NewEventDto;
import ru.practicum.stats.exception.NotFoundException;
import ru.practicum.stats.exception.ValidationException;
import ru.practicum.stats.mapper.EventMapper;
import ru.practicum.stats.mapper.LocationMapper;
import ru.practicum.stats.model.Category;
import ru.practicum.stats.model.Event;
import ru.practicum.stats.model.User;
import ru.practicum.stats.model.enums.EventState;
import ru.practicum.stats.repository.CategoryRepository;
import ru.practicum.stats.repository.EventRepository;
import ru.practicum.stats.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;
    private final StatsClient statsClient;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String APP_NAME = "ewm-main-service";

    @Override
    @Transactional
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        log.info("Добавление нового события пользователем с id: {}", userId);

        User initiator = userRepository.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() ->
                        new NotFoundException("Категория с id=" + newEventDto.getCategory() + " не найдена"));

        LocalDateTime eventDate;
        try {
            eventDate = LocalDateTime.parse(newEventDto.getEventDate(), FORMATTER);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Неверный формат даты. Ожидается: yyyy-MM-dd HH:mm:ss");
        }

        LocalDateTime now = LocalDateTime.now();
        if (eventDate.isBefore(now.plusHours(2))) {
            throw new ValidationException("Дата события должна быть не раньше чем через 2 часа от текущего момента");
        }

        Event event = eventMapper.toEntity(newEventDto);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setState(EventState.PENDING);
        event.setCreatedOn(now);
        event.setConfirmedRequests(0L);
        event.setViews(0L);

        if (newEventDto.getLocation() != null) {
            event.setLocation(locationMapper.toEntity(newEventDto.getLocation()));
        }

        if (event.getPaid() == null) {
            event.setPaid(false);
        }
        if (event.getParticipantLimit() == null) {
            event.setParticipantLimit(0);
        }
        if (event.getRequestModeration() == null) {
            event.setRequestModeration(true);
        }

        Event savedEvent = eventRepository.save(event);
        log.info("Событие успешно добавлено с id: {}", savedEvent.getId());

        return eventMapper.toFullDto(savedEvent);
    }

    @Override
    public List<EventShortDto> getEventsByUser(Long userId, int from, int size) {
        log.info("Получение событий пользователя с id: {}, from={}, size={}", userId, from, size);

        if (from < 0) {
            throw new ValidationException("Параметр 'from' не может быть отрицательным");
        }
        if (size <= 0) {
            throw new ValidationException("Параметр 'size' должен быть положительным");
        }

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        Pageable pageable = PageRequest.of(from / size, size);

        List<Event> events = eventRepository.findByInitiatorId(userId, pageable);

        Map<Long, Long> viewsMap = getViewsForEvents(events);
        events.forEach(event ->
                event.setViews(viewsMap.getOrDefault(event.getId(), 0L))
        );

        log.info("Найдено {} событий для пользователя с id: {}", events.size(), userId);

        return events.stream()
                .map(eventMapper::toShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEvent(Long id, HttpServletRequest request) {
        log.info("Получение события с id: {}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Событие с id " + id + " не найдено"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Событие с id " + id + " не найдено");
        }

        saveHitAsync(request);

        Long views = getViewsForEvent(id);
        event.setViews(views);

        log.info("Событие с id: {} успешно получено, просмотров: {}", id, views);

        return eventMapper.toFullDto(event);
    }

    @Override
    public List<EventShortDto> getEvents(EventSearchParams params, HttpServletRequest request) {
        log.info("Поиск событий с параметрами: {}", params);

        LocalDateTime rangeStart = params.getRangeStart();
        LocalDateTime rangeEnd = params.getRangeEnd();

        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(100);
        }

        int from = params.getFrom() != null ? params.getFrom() : 0;
        int size = params.getSize() != null ? params.getSize() : 10;
        Pageable pageable = PageRequest.of(from / size, size);

        List<Event> events = eventRepository.findPublishedEvents(
                EventState.PUBLISHED,
                params.getText(),
                params.getCategories(),
                params.getPaid(),
                rangeStart,
                rangeEnd,
                params.getOnlyAvailable() != null ? params.getOnlyAvailable() : false,
                params.getSort(),
                pageable
        );

        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Long> viewsMap = getViewsForEvents(events);

        events.forEach(event ->
                event.setViews(viewsMap.getOrDefault(event.getId(), 0L))
        );

        if (request != null) {
            saveHitAsync(request);
        }

        log.info("Найдено {} событий", events.size());

        return events.stream()
                .map(eventMapper::toShortDto)
                .collect(Collectors.toList());
    }

    /**
     * Сохраняет информацию о просмотре в сервисе статистики
     */
    private void saveHit(HttpServletRequest request) {
        try {
            EndpointHit hit = EndpointHit.builder()
                    .app(APP_NAME)
                    .uri(request.getRequestURI())
                    .ip(getClientIp(request))
                    .timestamp(LocalDateTime.now())
                    .build();

            statsClient.hit(hit);
            log.debug("Просмотр сохранен: {}", hit);
        } catch (Exception e) {
            log.error("Ошибка при сохранении статистики: {}", e.getMessage());
        }
    }

    /**
     * Асинхронно сохраняет информацию о просмотре
     */
    private void saveHitAsync(HttpServletRequest request) {
        try {
            EndpointHit hit = EndpointHit.builder()
                    .app(APP_NAME)
                    .uri(request.getRequestURI())
                    .ip(getClientIp(request))
                    .timestamp(LocalDateTime.now())
                    .build();

            statsClient.hitAsync(hit);
            log.debug("Асинхронное сохранение просмотра: {}", hit);
        } catch (Exception e) {
            log.error("Ошибка при асинхронном сохранении статистики: {}", e.getMessage());
        }
    }

    /**
     * Получает количество просмотров для одного события
     */
    private Long getViewsForEvent(Long eventId) {
        try {
            LocalDateTime start = LocalDateTime.now().minusYears(10); // Смотрим за последние 10 лет
            LocalDateTime end = LocalDateTime.now();
            String uri = "/events/" + eventId;

            List<ViewStats> stats = statsClient.getStats(start, end, List.of(uri), false);

            if (!stats.isEmpty()) {
                return stats.getFirst().getHits();
            }
        } catch (Exception e) {
            log.error("Ошибка при получении статистики для события {}: {}", eventId, e.getMessage());
        }
        return 0L;
    }

    /**
     * Получает просмотры для нескольких событий
     */
    private Map<Long, Long> getViewsForEvents(List<Event> events) {
        if (events.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            List<String> uris = events.stream()
                    .map(event -> "/events/" + event.getId())
                    .collect(Collectors.toList());

            LocalDateTime start = LocalDateTime.now().minusYears(10);
            LocalDateTime end = LocalDateTime.now();

            List<ViewStats> stats = statsClient.getStats(start, end, uris, false);

            return stats.stream()
                    .filter(stat -> stat.getUri() != null && stat.getUri().startsWith("/events/"))
                    .collect(Collectors.toMap(
                            stat -> extractEventId(stat.getUri()),
                            ViewStats::getHits,
                            (v1, v2) -> v1
                    ));

        } catch (Exception e) {
            log.error("Ошибка при получении статистики для списка событий: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * Извлекает ID события из URI
     */
    private Long extractEventId(String uri) {
        try {
            String[] parts = uri.split("/");
            return Long.parseLong(parts[parts.length - 1]);
        } catch (Exception e) {
            log.error("Ошибка при извлечении ID события из URI: {}", uri);
            return -1L;
        }
    }

    /**
     * Получает IP клиента из request
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}