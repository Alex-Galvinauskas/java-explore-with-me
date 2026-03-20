package ru.practicum.stats.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.stats.model.EndpointHitEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class StatsRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private StatsRepository statsRepository;
    private LocalDateTime now;
    private LocalDateTime hourAgo;
    private LocalDateTime twoHoursAgo;
    private LocalDateTime hourLater;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        hourAgo = now.minusHours(1);
        twoHoursAgo = now.minusHours(2);
        hourLater = now.plusHours(1);
        statsRepository.deleteAll();
    }

    @Test
    void shouldGetStatsWithUniqueIp() {
        createHit("ewm-main-service", "/events/1", "192.168.1.1", hourAgo);
        createHit("ewm-main-service", "/events/1",
                "192.168.1.1", hourAgo.plusMinutes(5));
        createHit("ewm-main-service", "/events/1", "192.168.1.2", hourAgo);
        createHit("ewm-main-service", "/events/2", "192.168.1.1", hourAgo);
        createHit("ewm-main-service", "/events/2", "192.168.1.3", hourAgo);
        List<ViewStats> stats = statsRepository.getStats(
                twoHoursAgo,
                now,
                List.of("/events/1", "/events/2"),
                true
        );
        assertThat(stats).hasSize(2);
        ViewStats event1Stats = stats.stream()
                .filter(s -> s.getUri().equals("/events/1"))
                .findFirst()
                .orElseThrow();
        assertThat(event1Stats.getHits()).isEqualTo(2);
        ViewStats event2Stats = stats.stream()
                .filter(s -> s.getUri().equals("/events/2"))
                .findFirst()
                .orElseThrow();
        assertThat(event2Stats.getHits()).isEqualTo(2);
    }

    @Test
    void shouldGetStatsWithNonUniqueIp() {
        createHit("ewm-main-service", "/events/1", "192.168.1.1", hourAgo);
        createHit("ewm-main-service", "/events/1",
                "192.168.1.1", hourAgo.plusMinutes(5));
        createHit("ewm-main-service", "/events/1", "192.168.1.2", hourAgo);
        createHit("ewm-main-service", "/events/2", "192.168.1.1", hourAgo);
        List<ViewStats> stats = statsRepository.getStats(
                twoHoursAgo,
                now,
                List.of("/events/1", "/events/2"),
                false
        );
        assertThat(stats).hasSize(2);
        ViewStats event1Stats = stats.stream()
                .filter(s -> s.getUri().equals("/events/1"))
                .findFirst()
                .orElseThrow();
        assertThat(event1Stats.getHits()).isEqualTo(3);
        ViewStats event2Stats = stats.stream()
                .filter(s -> s.getUri().equals("/events/2"))
                .findFirst()
                .orElseThrow();
        assertThat(event2Stats.getHits()).isEqualTo(1);
    }

    @Test
    void shouldFilterByDateRange() {
        createHit("ewm-main-service", "/events/1", "192.168.1.1", twoHoursAgo);
        createHit("ewm-main-service", "/events/1", "192.168.1.1", hourAgo);
        createHit("ewm-main-service", "/events/1", "192.168.1.1", now);
        createHit("ewm-main-service", "/events/1", "192.168.1.1", hourLater);
        List<ViewStats> stats = statsRepository.getStats(
                hourAgo.minusMinutes(1),
                now.plusMinutes(1),
                null,
                false
        );
        assertThat(stats).hasSize(1);
        assertThat(stats.getFirst().getHits()).isEqualTo(2);
    }

    @Test
    void shouldFilterByUris() {
        createHit("ewm-main-service", "/events/1", "192.168.1.1", hourAgo);
        createHit("ewm-main-service", "/events/2", "192.168.1.1", hourAgo);
        createHit("ewm-main-service", "/events/3", "192.168.1.1", hourAgo);
        createHit("ewm-main-service", "/events/4", "192.168.1.1", hourAgo);
        List<ViewStats> stats = statsRepository.getStats(
                twoHoursAgo,
                now,
                List.of("/events/1", "/events/2", "/events/3"),
                false
        );
        assertThat(stats).hasSize(3);
        assertThat(stats).allMatch(s ->
                s.getUri().equals("/events/1") ||
                        s.getUri().equals("/events/2") ||
                        s.getUri().equals("/events/3")
        );
    }

    @Test
    void shouldReturnEmptyListWhenNoData() {
        List<ViewStats> stats = statsRepository.getStats(
                twoHoursAgo,
                now,
                List.of("/events/1"),
                false
        );
        assertThat(stats).isEmpty();
    }

    @Test
    void shouldHandleNullUrisParameter() {
        createHit("ewm-main-service", "/events/1", "192.168.1.1", hourAgo);
        createHit("ewm-main-service", "/events/2", "192.168.1.1", hourAgo);
        List<ViewStats> stats = statsRepository.getStats(
                twoHoursAgo,
                now,
                null,
                false
        );
        assertThat(stats).hasSize(2);
    }

    @Test
    void shouldOrderByHitsDescending() {
        createHit("ewm-main-service", "/events/1", "192.168.1.1", hourAgo);
        createHit("ewm-main-service", "/events/2", "192.168.1.1", hourAgo);
        createHit("ewm-main-service", "/events/2", "192.168.1.2", hourAgo);
        createHit("ewm-main-service", "/events/3", "192.168.1.1", hourAgo);
        createHit("ewm-main-service", "/events/3", "192.168.1.2", hourAgo);
        createHit("ewm-main-service", "/events/3", "192.168.1.3", hourAgo);
        List<ViewStats> stats = statsRepository.getStats(
                twoHoursAgo,
                now,
                null,
                false
        );
        assertThat(stats).hasSize(3);
        assertThat(stats.get(0).getUri()).isEqualTo("/events/3");
        assertThat(stats.get(0).getHits()).isEqualTo(3);
        assertThat(stats.get(1).getUri()).isEqualTo("/events/2");
        assertThat(stats.get(1).getHits()).isEqualTo(2);
        assertThat(stats.get(2).getUri()).isEqualTo("/events/1");
        assertThat(stats.get(2).getHits()).isEqualTo(1);
    }

    private void createHit(String app, String uri, String ip, LocalDateTime timestamp) {
        EndpointHitEntity hit = EndpointHitEntity.builder()
                .app(app)
                .uri(uri)
                .ip(ip)
                .timestamp(timestamp)
                .build();
        entityManager.persist(hit);
    }
}