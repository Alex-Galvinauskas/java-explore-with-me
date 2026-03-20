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

    public void saveHit(HttpServletRequest request) {
        if (request == null) return;

        try {
            EndpointHit hit = buildEndpointHit(request);
            statsClient.hit(hit);
            log.debug("Сохранение просмотра: {}", hit);
        } catch (Exception e) {
            log.error("Ошибка при сохранении статистики: {}", e.getMessage());
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
        if (eventIds == null || eventIds.isEmpty()) {
            log.debug("Пустой список eventIds для получения просмотров");
            return Collections.emptyMap();
        }

        try {
            List<String> uris = buildEventUris(eventIds);

            if (uris.isEmpty()) {
                log.debug("Пустой список uris для получения просмотров");
                return Collections.emptyMap();
            }

            List<ViewStats> stats = fetchStatsForUris(uris);

            return stats.stream()
                    .filter(stat -> stat != null && stat.getUri() != null && stat.getUri().startsWith("/events/"))
                    .collect(Collectors.toMap(
                            stat -> extractEventId(stat.getUri()),
                            ViewStats::getHits,
                            (v1, v2) -> v1
                    ));
        } catch (Exception e) {
            log.error("Ошибка при получении статистики для списка событий: {}", e.getMessage(), e);
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
        try {
            if (uris == null || uris.isEmpty()) {
                log.debug("Пустой список uris для запроса статистики");
                return Collections.emptyList();
            }

            LocalDateTime start = LocalDateTime.now().minusYears(10);
            LocalDateTime end = LocalDateTime.now();

            List<ViewStats> stats = statsClient.getStats(start, end, uris, false);

            return stats != null ? stats : Collections.emptyList();
        } catch (Exception e) {
            log.error("Ошибка при запросе статистики для uris {}: {}", uris, e.getMessage());
            return Collections.emptyList();
        }
    }

    private Long extractEventId(String uri) {
        try {
            String[] parts = uri.split("/");
            if (parts.length > 0) {
                return Long.parseLong(parts[parts.length - 1]);
            }
            return -1L;
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