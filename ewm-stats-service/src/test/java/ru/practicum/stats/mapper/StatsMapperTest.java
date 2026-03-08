package ru.practicum.stats.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.mapper.impl.StatsMapperImpl;
import ru.practicum.stats.model.EndpointHitEntity;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

class StatsMapperTest {
    private final StatsMapper mapper = new StatsMapperImpl();

    @Test
    void shouldMapDtoToEntity() {
        LocalDateTime now = LocalDateTime.now();
        EndpointHit dto = EndpointHit.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(now)
                .build();
        EndpointHitEntity entity = mapper.toEntity(dto);
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull();
        assertThat(entity.getApp()).isEqualTo(dto.getApp());
        assertThat(entity.getUri()).isEqualTo(dto.getUri());
        assertThat(entity.getIp()).isEqualTo(dto.getIp());
        assertThat(entity.getTimestamp()).isEqualTo(dto.getTimestamp());
    }

    @Test
    void shouldMapEntityToDto() {
        LocalDateTime now = LocalDateTime.now();
        EndpointHitEntity entity = EndpointHitEntity.builder()
                .id(1L)
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(now)
                .build();
        EndpointHit dto = mapper.toDto(entity);
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(entity.getId());
        assertThat(dto.getApp()).isEqualTo(entity.getApp());
        assertThat(dto.getUri()).isEqualTo(entity.getUri());
        assertThat(dto.getIp()).isEqualTo(entity.getIp());
        assertThat(dto.getTimestamp()).isEqualTo(entity.getTimestamp());
    }

    @Test
    void shouldHandleNullValues() {
        EndpointHit dto = EndpointHit.builder().build();
        EndpointHitEntity entity = mapper.toEntity(dto);
        assertThat(entity).isNotNull();
        assertThat(entity.getApp()).isNull();
        assertThat(entity.getUri()).isNull();
        assertThat(entity.getIp()).isNull();
        assertThat(entity.getTimestamp()).isNull();
    }
}