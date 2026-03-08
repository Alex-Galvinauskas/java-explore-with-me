package ru.practicum.stats.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.stats.exception.StatsValidationException;
import ru.practicum.stats.mapper.StatsMapper;
import ru.practicum.stats.model.EndpointHitEntity;
import ru.practicum.stats.repository.StatsRepository;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsServiceImplTest {
    @Mock
    private StatsRepository statsRepository;
    @Mock
    private StatsMapper statsMapper;
    @InjectMocks
    private StatsServiceImpl statsService;
    private LocalDateTime now;
    private LocalDateTime start;
    private LocalDateTime end;
    private EndpointHit hitDto;
    private EndpointHitEntity hitEntity;
    private ViewStats viewStats;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        start = now.minusDays(1);
        end = now.plusDays(1);
        hitDto = EndpointHit.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(now)
                .build();
        hitEntity = EndpointHitEntity.builder()
                .id(1L)
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(now)
                .build();
        viewStats = ViewStats.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .hits(5L)
                .build();
    }

    @Test
    void shouldSaveHit() {
        when(statsMapper.toEntity(hitDto)).thenReturn(hitEntity);
        when(statsRepository.save(any(EndpointHitEntity.class))).thenReturn(hitEntity);
        statsService.hit(hitDto);
        verify(statsMapper, times(1)).toEntity(hitDto);
        verify(statsRepository, times(1)).save(hitEntity);
    }

    @Test
    void shouldSetCurrentTimestampIfNotProvided() {
        EndpointHit hitWithoutTimestamp = EndpointHit.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .build();
        EndpointHitEntity entityWithoutTimestamp = EndpointHitEntity.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .build();
        when(statsMapper.toEntity(hitWithoutTimestamp)).thenReturn(entityWithoutTimestamp);
        when(statsRepository.save(any(EndpointHitEntity.class)))
                .thenAnswer(invocation -> {
                    EndpointHitEntity saved = invocation.getArgument(0);
                    saved.setId(1L);
                    return saved;
                });
        statsService.hit(hitWithoutTimestamp);
        verify(statsRepository).save(argThat(entity ->
                entity.getTimestamp() != null &&
                        entity.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1))
        ));
    }

    @Test
    void shouldGetStatsWithFilters() {
        List<String> uris = List.of("/events/1", "/events/2");
        List<ViewStats> expectedStats = List.of(viewStats);
        when(statsRepository.getStats(start, end, uris, false)).thenReturn(expectedStats);
        List<ViewStats> actualStats = statsService.getStats(start, end, uris, false);
        assertThat(actualStats).isEqualTo(expectedStats);
        verify(statsRepository, times(1))
                .getStats(start, end, uris, false);
    }

    @Test
    void shouldConvertEmptyUrisToNull() {
        List<String> emptyUris = List.of();
        List<ViewStats> expectedStats = List.of(viewStats);
        when(statsRepository.getStats(start, end, null, false)).thenReturn(expectedStats);
        List<ViewStats> actualStats = statsService.getStats(start, end, emptyUris, false);
        assertThat(actualStats).isEqualTo(expectedStats);
        verify(statsRepository, times(1))
                .getStats(start, end, null, false);
    }

    @Test
    void shouldGetAllStats() {
        List<ViewStats> expectedStats = List.of(viewStats);
        when(statsRepository.getStatsAll(start, end, false)).thenReturn(expectedStats);
        List<ViewStats> actualStats = statsService.getStatsAll(start, end, false);
        assertThat(actualStats).isEqualTo(expectedStats);
        verify(statsRepository, times(1)).getStatsAll(start, end, false);
    }

    @Test
    void shouldThrowExceptionWhenStartIsNull() {
        assertThatThrownBy(() ->
                statsService.getStats(null, end, null, false))
                .isInstanceOf(StatsValidationException.class)
                .hasMessageContaining("Даты начала и конца должны быть указаны");
    }

    @Test
    void shouldThrowExceptionWhenEndIsNull() {
        assertThatThrownBy(() ->
                statsService.getStats(start, null, null, false))
                .isInstanceOf(StatsValidationException.class)
                .hasMessageContaining("Даты начала и конца должны быть указаны");
    }

    @Test
    void shouldThrowExceptionWhenStartIsAfterEnd() {
        LocalDateTime invalidStart = end.plusDays(1);
        assertThatThrownBy(() ->
                statsService.getStats(invalidStart, end, null, false))
                .isInstanceOf(StatsValidationException.class)
                .hasMessageContaining("не может быть позже");
    }

    @Test
    void shouldHandleNullUris() {
        List<ViewStats> expectedStats = List.of(viewStats);
        when(statsRepository.getStats(start, end, null, true)).thenReturn(expectedStats);
        List<ViewStats> actualStats = statsService.getStats(start, end, null, true);
        assertThat(actualStats).isEqualTo(expectedStats);
        verify(statsRepository, times(1))
                .getStats(start, end, null, true);
    }

    @Test
    void shouldHandleEmptyStatsList() {
        when(statsRepository.getStats(start, end, null, false)).thenReturn(List.of());
        List<ViewStats> actualStats = statsService.getStats(start, end, null, false);
        assertThat(actualStats).isEmpty();
    }
}