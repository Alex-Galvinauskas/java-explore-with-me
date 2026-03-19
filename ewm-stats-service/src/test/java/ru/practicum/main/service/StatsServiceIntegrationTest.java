package ru.practicum.main.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.EndpointHit;
import ru.practicum.main.dto.ViewStats;
import ru.practicum.main.exception.StatsValidationException;
import ru.practicum.main.repository.StatsRepository;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StatsServiceIntegrationTest {
    @Autowired
    private StatsService statsService;
    @Autowired
    private StatsRepository statsRepository;
    private LocalDateTime now;
    private LocalDateTime hourAgo;
    private LocalDateTime twoHoursAgo;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        hourAgo = now.minusHours(1);
        twoHoursAgo = now.minusHours(2);
        statsRepository.deleteAll();
    }

    @Test
    void shouldSaveAndRetrieveStats() {
        EndpointHit hit1 = createHit("/events/1", "192.168.1.1", hourAgo);
        EndpointHit hit2 = createHit("/events/1", "192.168.1.2", hourAgo);
        EndpointHit hit3 = createHit("/events/2", "192.168.1.1", hourAgo);
        statsService.hit(hit1);
        statsService.hit(hit2);
        statsService.hit(hit3);
        List<ViewStats> nonUniqueStats = statsService.getStats(
                twoHoursAgo, now, List.of("/events/1", "/events/2"), false
        );
        assertThat(nonUniqueStats).hasSize(2);
        ViewStats event1Stats = findStatsByUri(nonUniqueStats, "/events/1");
        assertThat(event1Stats.getHits()).isEqualTo(2);
        ViewStats event2Stats = findStatsByUri(nonUniqueStats, "/events/2");
        assertThat(event2Stats.getHits()).isEqualTo(1);
        List<ViewStats> uniqueStats = statsService.getStats(
                twoHoursAgo, now, List.of("/events/1", "/events/2"), true
        );
        event1Stats = findStatsByUri(uniqueStats, "/events/1");
        assertThat(event1Stats.getHits()).isEqualTo(2);
        event2Stats = findStatsByUri(uniqueStats, "/events/2");
        assertThat(event2Stats.getHits()).isEqualTo(1);
    }

    @Test
    void shouldNotSaveHitWithInvalidData() {
        EndpointHit invalidHit = EndpointHit.builder()
                .app("")
                .uri("/events/1")
                .ip("invalid-ip")
                .timestamp(now)
                .build();
        assertThatThrownBy(() -> statsService.hit(invalidHit))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldReturnEmptyListWhenNoStats() {
        List<ViewStats> stats = statsService.getStats(twoHoursAgo, now, null, false);
        assertThat(stats).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenDatesInvalid() {
        LocalDateTime invalidStart = now.plusDays(1);
        LocalDateTime invalidEnd = now.minusDays(1);
        assertThatThrownBy(() ->
                statsService.getStats(invalidStart, invalidEnd, null, false))
                .isInstanceOf(StatsValidationException.class)
                .hasMessageContaining("не может быть позже");
    }

    @Test
    void shouldHandleMultipleServices() {
        EndpointHit hit1 = createHit("service-1", "/events/1", "192.168.1.1", hourAgo);
        EndpointHit hit2 = createHit("service-1", "/events/1", "192.168.1.2", hourAgo);
        EndpointHit hit3 = createHit("service-2", "/events/1", "192.168.1.1", hourAgo);
        statsService.hit(hit1);
        statsService.hit(hit2);
        statsService.hit(hit3);
        List<ViewStats> stats = statsService.getStats(twoHoursAgo, now, null, false);
        assertThat(stats).hasSize(2);
        assertThat(stats).anyMatch(s ->
                s.getApp().equals("service-1") &&
                        s.getUri().equals("/events/1") &&
                        s.getHits() == 2
        );
        assertThat(stats).anyMatch(s ->
                s.getApp().equals("service-2") &&
                        s.getUri().equals("/events/1") &&
                        s.getHits() == 1
        );
    }

    private EndpointHit createHit(String uri, String ip, LocalDateTime timestamp) {
        return createHit("ewm-main-service", uri, ip, timestamp);
    }

    private EndpointHit createHit(String app, String uri, String ip, LocalDateTime timestamp) {
        return EndpointHit.builder()
                .app(app)
                .uri(uri)
                .ip(ip)
                .timestamp(timestamp)
                .build();
    }

    private ViewStats findStatsByUri(List<ViewStats> stats, String uri) {
        return stats.stream()
                .filter(s -> s.getUri().equals(uri))
                .findFirst()
                .orElseThrow();
    }
}