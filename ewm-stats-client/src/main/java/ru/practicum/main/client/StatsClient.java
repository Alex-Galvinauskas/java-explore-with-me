package ru.practicum.main.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.main.dto.EndpointHit;
import ru.practicum.main.dto.ViewStats;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class StatsClient {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1000;

    private final String serverUrl;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final AtomicInteger successfulRequests = new AtomicInteger(0);
    private final AtomicInteger failedRequests = new AtomicInteger(0);
    private final AtomicInteger retryAttempts = new AtomicInteger(0);

    @Autowired
    public StatsClient(@Value("${stats-server.url}") String serverUrl,
                       RestTemplate restTemplate,
                       ObjectMapper objectMapper) {
        this.serverUrl = serverUrl;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Синхронная отправка статистики с механизмом повторных попыток
     */
    public void hit(EndpointHit hit) {
        executeWithRetryVoid(() -> {
            String url = serverUrl + "/hit";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<EndpointHit> requestEntity = new HttpEntity<>(hit, headers);

            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    Void.class
            );

            if (response.getStatusCode() == HttpStatus.CREATED) {
                successfulRequests.incrementAndGet();
                log.debug("Статистика успешно отправлена: {}", hit);
            } else {
                failedRequests.incrementAndGet();
                log.warn("Неожиданный статус ответа: {}", response.getStatusCode());
            }
        });
    }

    /**
     * Асинхронная отправка статистики
     */
    @Async("statsExecutor")
    public CompletableFuture<Void> hitAsync(EndpointHit hit) {
        return CompletableFuture.runAsync(() -> {
            try {
                hit(hit);
                log.debug("Асинхронная отправка статистики выполнена успешно");
            } catch (Exception e) {
                log.error("Ошибка при асинхронной отправке статистики: {}", e.getMessage());
                throw new RuntimeException("Ошибка асинхронной отправки статистики", e);
            }
        });
    }

    /**
     * Пакетная асинхронная отправка статистики
     */
    @Async("statsExecutor")
    public CompletableFuture<List<Void>> hitBatchAsync(List<EndpointHit> hits) {
        return CompletableFuture.supplyAsync(() ->
                hits.stream()
                        .map(hit -> {
                            try {
                                hit(hit);
                                return (Void) null;
                            } catch (Exception e) {
                                log.error("Ошибка при отправке хита в пакете: {}", hit);
                                return null;
                            }
                        })
                        .toList()
        );
    }

    /**
     * Получение статистики с механизмом повторных попыток
     */
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end,
                                    List<String> uris, Boolean unique) {
        return executeWithRetry(() -> {
            try {
                String encodedStart = encodeDateTime(start);
                String encodedEnd = encodeDateTime(end);

                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverUrl + "/stats")
                        .queryParam("start", encodedStart)
                        .queryParam("end", encodedEnd);

                if (uris != null && !uris.isEmpty()) {
                    uris.forEach(uri -> builder.queryParam("uris", uri));
                }

                if (unique != null) {
                    builder.queryParam("unique", unique);
                }

                String url = builder.build(false).toUriString();
                log.debug("Запрос статистики по URL: {}", url);

                HttpHeaders headers = new HttpHeaders();
                headers.setAccept(List.of(MediaType.APPLICATION_JSON));

                HttpEntity<?> entity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        String.class
                );

                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    successfulRequests.incrementAndGet();
                    return objectMapper.readValue(
                            response.getBody(),
                            new TypeReference<List<ViewStats>>() {}
                    );
                } else {
                    failedRequests.incrementAndGet();
                    log.warn("Неожиданный статус ответа при получении статистики: {}", response.getStatusCode());
                    return Collections.emptyList();
                }
            } catch (Exception e) {
                failedRequests.incrementAndGet();
                log.error("Ошибка при получении статистики: {}", e.getMessage());
                throw new RuntimeException("Ошибка при получении статистики", e);
            }
        }, "getStats");
    }

    /**
     * Асинхронное получение статистики
     */
    @Async("statsExecutor")
    public CompletableFuture<List<ViewStats>> getStatsAsync(LocalDateTime start, LocalDateTime end,
                                                            List<String> uris, Boolean unique) {
        return CompletableFuture.supplyAsync(() -> getStats(start, end, uris, unique));
    }

    /**
     * Метод с механизмом повторных попыток
     */
    private <T> T executeWithRetry(SupplierWithException<T> supplier, String operationName) {
        int attempt = 0;
        Exception lastException = null;

        while (attempt < MAX_RETRY_ATTEMPTS) {
            try {
                return supplier.get();
            } catch (ResourceAccessException e) {
                lastException = e;
                attempt++;
                retryAttempts.incrementAndGet();

                if (attempt < MAX_RETRY_ATTEMPTS) {
                    log.warn("Попытка {} для операции {} не удалась из-за проблем с сетью. " +
                            "Повтор через {} мс", attempt, operationName, RETRY_DELAY_MS);
                    try {
                        Thread.sleep(RETRY_DELAY_MS * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Прервано во время ожидания повторной попытки", ie);
                    }
                }
            } catch (RestClientResponseException e) {
                log.error("Ошибка ответа от сервера при операции {}: статус {}, тело: {}",
                        operationName, e.getStatusCode(), e.getResponseBodyAsString());
                throw new RuntimeException("Ошибка сервера: " + e.getStatusCode(), e);
            } catch (Exception e) {
                log.error("Неожиданная ошибка при операции {}: {}", operationName, e.getMessage());
                throw new RuntimeException("Неожиданная ошибка", e);
            }
        }

        log.error("Все {} попыток для операции {} исчерпаны", MAX_RETRY_ATTEMPTS, operationName);
        throw new RuntimeException("Не удалось выполнить операцию " + operationName +
                " после " + MAX_RETRY_ATTEMPTS + " попыток", lastException);
    }

    /**
     * Кодирование даты для URL
     */
    private String encodeDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        String formatted = dateTime.format(FORMATTER);
        return URLEncoder.encode(formatted, StandardCharsets.UTF_8);
    }

    /**
     * Получение статистики клиента (для мониторинга)
     */
    public ClientStatsMonitoring getClientStats() {
        return ClientStatsMonitoring.builder()
                .successfulRequests(successfulRequests.get())
                .failedRequests(failedRequests.get())
                .retryAttempts(retryAttempts.get())
                .serverUrl(serverUrl)
                .build();
    }

    private void executeWithRetryVoid(RunnableWithException runnable) {
        executeWithRetry(() -> {
            runnable.run();
            return null;
        }, "hit");
    }

    @FunctionalInterface
    private interface RunnableWithException {
        void run() throws Exception;
    }

    @FunctionalInterface
    private interface SupplierWithException<T> {
        T get() throws Exception;
    }
}