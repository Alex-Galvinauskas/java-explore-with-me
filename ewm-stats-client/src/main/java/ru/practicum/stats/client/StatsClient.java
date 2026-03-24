package ru.practicum.stats.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class StatsClient {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String serverUrl;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final AtomicInteger successfulRequests = new AtomicInteger(0);
    private final AtomicInteger failedRequests = new AtomicInteger(0);

    @Autowired
    public StatsClient(@Value("${stats-server.url}") String serverUrl,
                       RestTemplate restTemplate,
                       ObjectMapper objectMapper) {
        this.serverUrl = serverUrl;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public void hitSync(EndpointHit hit) {
        try {
            String url = serverUrl + "/hit";
            log.info("Отправка hit в stats-service: {} тело: {}", url, hit);

            HttpEntity<EndpointHit> requestEntity = new HttpEntity<>(hit);
            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    Void.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                successfulRequests.incrementAndGet();
                log.info("Hit успешно сохранён со статусом: {}", response.getStatusCode());
            } else {
                failedRequests.incrementAndGet();
                log.error("Не удалось сохранить hit, статус: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            failedRequests.incrementAndGet();
            log.error("Ошибка при сохранении hit: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось сохранить hit", e);
        }
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end,
                                    List<String> uris, Boolean unique) {
        try {
            String formattedStart = start.format(FORMATTER);
            String formattedEnd = end.format(FORMATTER);

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverUrl + "/stats")
                    .queryParam("start", formattedStart)
                    .queryParam("end", formattedEnd);

            if (uris != null && !uris.isEmpty()) {
                for (String uri : uris) {
                    builder.queryParam("uris", uri);
                }
            }

            if (unique != null) {
                builder.queryParam("unique", unique);
            }

            String url = builder.build().toUriString();
            log.info("Получение статистики из: {}", url);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            log.info("Статус ответа: {}, тело: {}", response.getStatusCode(), response.getBody());

            if (response.getBody() != null) {
                return objectMapper.readValue(
                        response.getBody(),
                        new TypeReference<List<ViewStats>>() {}
                );
            }

            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Ошибка при получении статистики: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Получение метрик клиента
     */
    public ClientStatsMonitoring getMonitoringStats() {
        return ClientStatsMonitoring.builder()
                .successfulRequests(successfulRequests.get())
                .failedRequests(failedRequests.get())
                .retryAttempts(0)
                .serverUrl(serverUrl)
                .build();
    }
}