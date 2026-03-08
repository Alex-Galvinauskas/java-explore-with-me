package ru.practicum.stats.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ViewStatsTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        ViewStats stats = ViewStats.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .hits(10L)
                .build();

        String json = objectMapper.writeValueAsString(stats);

        assertThat(json).contains("\"app\":\"ewm-main-service\"");
        assertThat(json).contains("\"uri\":\"/events/1\"");
        assertThat(json).contains("\"hits\":10");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        String json = "{\"app\":\"ewm-main-service\",\"uri\":\"/events/1\",\"hits\":10}";

        ViewStats stats = objectMapper.readValue(json, ViewStats.class);

        assertThat(stats.getApp()).isEqualTo("ewm-main-service");
        assertThat(stats.getUri()).isEqualTo("/events/1");
        assertThat(stats.getHits()).isEqualTo(10L);
    }
}
