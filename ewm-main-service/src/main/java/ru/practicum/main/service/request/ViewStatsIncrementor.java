package ru.practicum.main.service.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.main.client.StatsClient;
import ru.practicum.main.dto.EndpointHit;
import ru.practicum.main.model.Event;
import ru.practicum.main.repository.EventRepository;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewStatsIncrementor {

    private final EventRepository eventRepository;
    private final StatsClient statsClient;
    private final RequestValidator requestValidator;

    public void incrementEventViews(Long eventId, String clientIp) {
        try {
            Event event = requestValidator.findEventById(eventId);
            updateEventViews(event);
            sendViewStats(eventId, clientIp);
        } catch (Exception e) {
            log.error("Ошибка при увеличении просмотров события {}: {}", eventId, e.getMessage(), e);
        }
    }

    private void updateEventViews(Event event) {
        event.setViews(event.getViews() != null ? event.getViews() + 1 : 1);
        eventRepository.save(event);
        log.debug("Увеличен счетчик просмотров для события {}: теперь {}",
                event.getId(), event.getViews());
    }

    private void sendViewStats(Long eventId, String clientIp) {
        EndpointHit hit = EndpointHit.builder()
                .app("ewm-main-service")
                .uri("/events/" + eventId)
                .ip(clientIp)
                .timestamp(LocalDateTime.now())
                .build();

        statsClient.hitAsync(hit);
        log.debug("Статистика просмотра отправлена для события {} с IP {}", eventId, clientIp);
    }
}