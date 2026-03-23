package ru.practicum.stats.statistics.event;

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

            log.debug("Сохранение хита для URI: {}", hit.getUri());
            statsClient.hitSync(hit);
        } catch (Exception e) {
            log.error("Ошибка при сохранении хита: {}", e.getMessage());
        }
    }

    public Map<Long, Long> getViewsForEvents(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            List<String> uris = eventIds.stream()
                    .map(id -> "/events/" + id)
                    .collect(Collectors.toList());

            LocalDateTime start = STATS_START;
            LocalDateTime end = STATS_END;

            if (start.isAfter(end)) {
                log.warn("Дата начала {} позже даты окончания {}, меняем их местами", start, end);
                LocalDateTime temp = start;
                start = end;
                end = temp;
            }

            List<ViewStats> stats = statsClient.getStats(start, end, uris, true);

            log.debug("Получена статистика для {} событий, записей: {}", eventIds.size(), stats.size());

            return stats.stream()
                    .collect(Collectors.toMap(
                            stat -> extractEventId(stat.getUri()),
                            ViewStats::getHits,
                            (v1, v2) -> v1
                    ));
        } catch (Exception e) {
            log.error("Ошибка при получении просмотров для событий: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    public Long getViewsForEvent(Long eventId) {
        return getViewsForEvents(List.of(eventId)).getOrDefault(eventId, 0L);
    }

    private Long extractEventId(String uri) {
        try {
            String[] parts = uri.split("/");
            return Long.parseLong(parts[parts.length - 1]);
        } catch (Exception e) {
            log.warn("Не удалось извлечь ID события из URI: {}", uri);
            return -1L;
        }
    }

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