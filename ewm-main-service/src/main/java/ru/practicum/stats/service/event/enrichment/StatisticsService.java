package ru.practicum.stats.service.event.enrichment;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final StatsClient statsClient;
    private static final String APP_NAME = "ewm-main-service";
    private static final long DEFAULT_VIEWS = 0L;

    public void saveHitAsync(HttpServletRequest request) {
        if (request == null) return;

        try {
            EndpointHit hit = buildEndpointHit(request);
            statsClient.hitAsync(hit);
            log.debug("Асинхронное сохранение просмотра: {}", hit);
        } catch (Exception e) {
            log.error("Ошибка при асинхронном сохранении статистики: {}", e.getMessage());
        }
    }

    public Long getViewsForEvent(Long eventId) {
        try {
            List<ViewStats> stats = fetchStatsForUris(List.of("/events/" + eventId));
            return stats.isEmpty() ? DEFAULT_VIEWS : stats.getFirst().getHits();
        } catch (Exception e) {
            log.error("Ошибка при получении статистики для события {}: {}", eventId, e.getMessage());
            return DEFAULT_VIEWS;
        }
    }

    public Map<Long, Long> getViewsForEvents(List<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            List<String> uris = buildEventUris(eventIds);
            List<ViewStats> stats = fetchStatsForUris(uris);

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

    private EndpointHit buildEndpointHit(HttpServletRequest request) {
        return EndpointHit.builder()
                .app(APP_NAME)
                .uri(request.getRequestURI())
                .ip(getClientIp(request))
                .timestamp(LocalDateTime.now())
                .build();
    }

    private List<String> buildEventUris(List<Long> eventIds) {
        return eventIds.stream()
                .map(id -> "/events/" + id)
                .collect(Collectors.toList());
    }

    private List<ViewStats> fetchStatsForUris(List<String> uris) {
        LocalDateTime start = LocalDateTime.now().minusYears(10);
        LocalDateTime end = LocalDateTime.now();
        return statsClient.getStats(start, end, uris, false);
    }

    private Long extractEventId(String uri) {
        try {
            String[] parts = uri.split("/");
            return Long.parseLong(parts[parts.length - 1]);
        } catch (Exception e) {
            log.error("Ошибка при извлечении ID события из URI: {}", uri);
            return -1L;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}