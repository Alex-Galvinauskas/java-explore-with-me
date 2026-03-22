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
    private static final LocalDateTime STATS_START =
            LocalDateTime.of(2000, 1, 1, 0, 0, 0);
    private static final LocalDateTime STATS_END =
            LocalDateTime.of(2100, 1, 1, 0, 0, 0);

    /**
     * Сохранение хита (синхронно)
     */
    public void saveHit(HttpServletRequest request) {
        if (request == null) {
            return;
        }

        try {
            EndpointHit hit = EndpointHit.builder()
                    .app(APP_NAME)
                    .uri(request.getRequestURI())
                    .ip(getClientIp(request))
                    .timestamp(LocalDateTime.now())
                    .build();

            log.debug("Saving hit for URI: {}", hit.getUri());
            statsClient.hitSync(hit);
        } catch (Exception e) {
            log.error("Error saving hit: {}", e.getMessage());
        }
    }

    /**
     * Получение просмотров для списка событий
     */
    public Map<Long, Long> getViewsForEvents(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            List<String> uris = eventIds.stream()
                    .map(id -> "/events/" + id)
                    .collect(Collectors.toList());

            // ✅ Добавляем проверку порядка дат
            LocalDateTime start = STATS_START;
            LocalDateTime end = STATS_END;

            if (start.isAfter(end)) {
                log.warn("Start date {} is after end date {}, swapping them", start, end);
                LocalDateTime temp = start;
                start = end;
                end = temp;
            }

            List<ViewStats> stats = statsClient.getStats(start, end, uris, true);

            log.debug("Retrieved stats for {} events, got {} records", eventIds.size(), stats.size());

            return stats.stream()
                    .collect(Collectors.toMap(
                            stat -> extractEventId(stat.getUri()),
                            ViewStats::getHits,
                            (v1, v2) -> v1
                    ));
        } catch (Exception e) {
            log.error("Error getting views for events: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * Получение просмотров для одного события
     */
    public Long getViewsForEvent(Long eventId) {
        return getViewsForEvents(List.of(eventId)).getOrDefault(eventId, 0L);
    }

    /**
     * Извлечение ID события из URI
     */
    private Long extractEventId(String uri) {
        try {
            String[] parts = uri.split("/");
            return Long.parseLong(parts[parts.length - 1]);
        } catch (Exception e) {
            log.warn("Failed to extract event ID from URI: {}", uri);
            return -1L;
        }
    }

    /**
     * Получение реального IP клиента с учетом прокси
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}