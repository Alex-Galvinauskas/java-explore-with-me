package ru.practicum.stats.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.ViewStats;

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

    /**
     * Асинхронная отправка статистики
     */
    @Async("statsExecutor")
    public CompletableFuture<Void> hitAsync(EndpointHit hit) {
        try {
            String url = serverUrl + "/hit";
            log.debug("Sending async hit to stats-service: {}", url);

            HttpEntity<EndpointHit> requestEntity = new HttpEntity<>(hit);
            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    Void.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                successfulRequests.incrementAndGet();
                log.debug("Hit saved successfully for uri: {}", hit.getUri());
            } else {
                failedRequests.incrementAndGet();
                log.warn("Failed to save hit for uri: {}, status: {}", hit.getUri(), response.getStatusCode());
            }
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            failedRequests.incrementAndGet();
            log.error("Error saving hit for uri: {}: {}", hit.getUri(), e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Синхронная отправка статистики (для критичных случаев)
     */
    public void hitSync(EndpointHit hit) {
        try {
            String url = serverUrl + "/hit";
            log.info("Sending hit to stats-service: {} with body: {}", url, hit);

            HttpEntity<EndpointHit> requestEntity = new HttpEntity<>(hit);
            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    Void.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                successfulRequests.incrementAndGet();
                log.info("Hit saved successfully with status: {}", response.getStatusCode());
            } else {
                failedRequests.incrementAndGet();
                log.error("Failed to save hit, status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            failedRequests.incrementAndGet();
            log.error("Error saving hit: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save hit", e);
        }
    }

    /**
     * Получение статистики для списка URI
     */
    public Map<String, Long> getStatsForUris(LocalDateTime start, LocalDateTime end, List<String> uris) {
        if (uris == null || uris.isEmpty()) {
            return Collections.emptyMap();
        }

        List<ViewStats> statsList = getStats(start, end, uris, true);
        Map<String, Long> statsMap = new HashMap<>();

        for (ViewStats stats : statsList) {
            statsMap.put(stats.getUri(), stats.getHits());
        }

        return statsMap;
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
            log.debug("Getting stats from: {}", url);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            log.debug("Response status: {}", response.getStatusCode());
            log.debug("Response body: {}", response.getBody());

            if (response.getBody() != null) {
                return objectMapper.readValue(
                        response.getBody(),
                        new TypeReference<List<ViewStats>>() {}
                );
            }

            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error getting stats: {}", e.getMessage(), e); // Добавить stack trace
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