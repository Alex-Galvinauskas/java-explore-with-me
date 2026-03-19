package ru.practicum.stats.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.stats.exception.ErrorHandler;
import ru.practicum.stats.service.StatsService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class StatsControllerTest {
    private MockMvc mockMvc;
    @Mock
    private StatsService statsService;
    @InjectMocks
    private StatsController statsController;
    private ObjectMapper objectMapper;
    private DateTimeFormatter formatter;
    private LocalDateTime now;
    private EndpointHit hitDto;
    private ViewStats viewStats;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        now = LocalDateTime.now();
        hitDto = EndpointHit.builder()
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
        mockMvc = MockMvcBuilders.standaloneSetup(statsController)
                .setControllerAdvice(new ErrorHandler())
                .build();
    }

    @Test
    void shouldSaveHit() throws Exception {
        doNothing().when(statsService).hit(any(EndpointHit.class));
        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hitDto)))
                .andExpect(status().isCreated());
        verify(statsService, times(1)).hit(any(EndpointHit.class));
    }

    @Test
    void shouldReturn400WhenHitDtoInvalid() throws Exception {
        EndpointHit invalidHit = EndpointHit.builder()
                .app("")
                .uri("/events/1")
                .ip("invalid-ip")
                .timestamp(now)
                .build();
        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidHit)))
                .andExpect(status().isBadRequest());
        verify(statsService, never()).hit(any(EndpointHit.class));
    }

    @Test
    void shouldGetStats() throws Exception {
        List<ViewStats> expectedStats = List.of(viewStats);
        when(statsService.getStats(any(LocalDateTime.class),
                any(LocalDateTime.class),
                anyList(), anyBoolean())).thenReturn(expectedStats);
        String startStr = now.minusDays(1).format(formatter);
        String endStr = now.plusDays(1).format(formatter);
        mockMvc.perform(get("/stats")
                        .param("start", startStr)
                        .param("end", endStr)
                        .param("uris", "/events/1")
                        .param("unique", "false"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].app").value("ewm-main-service"))
                .andExpect(jsonPath("$[0].uri").value("/events/1"))
                .andExpect(jsonPath("$[0].hits").value(5));
        verify(statsService, times(1))
                .getStats(any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(List.of("/events/1")), eq(false));
    }

    @Test
    void shouldGetStatsWithDefaultUnique() throws Exception {
        List<ViewStats> expectedStats = List.of(viewStats);
        when(statsService.getStats(any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(), anyBoolean())).thenReturn(expectedStats);
        String startStr = now.minusDays(1).format(formatter);
        String endStr = now.plusDays(1).format(formatter);
        mockMvc.perform(get("/stats")
                        .param("start", startStr)
                        .param("end", endStr))
                .andExpect(status().isOk());
        verify(statsService, times(1))
                .getStats(any(LocalDateTime.class),
                any(LocalDateTime.class), isNull(), eq(false));
    }

    @Test
    void shouldReturn400WhenStartDateMissing() throws Exception {
        mockMvc.perform(get("/stats")
                        .param("end", now.format(formatter)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenEndDateMissing() throws Exception {
        mockMvc.perform(get("/stats")
                        .param("start", now.format(formatter)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenInvalidDateFormat() throws Exception {
        mockMvc.perform(get("/stats")
                        .param("start", "invalid-date")
                        .param("end", "invalid-date"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetStatsWithMultipleUris() throws Exception {
        List<ViewStats> expectedStats = List.of(viewStats);
        when(statsService.getStats(any(LocalDateTime.class),
                any(LocalDateTime.class),
                anyList(), anyBoolean())).thenReturn(expectedStats);
        String startStr = now.minusDays(1).format(formatter);
        String endStr = now.plusDays(1).format(formatter);
        mockMvc.perform(get("/stats")
                        .param("start", startStr)
                        .param("end", endStr)
                        .param("uris", "/events/1", "/events/2"))
                .andExpect(status().isOk());
        verify(statsService, times(1)).getStats(any(LocalDateTime.class),
                any(LocalDateTime.class), eq(List.of("/events/1",
                        "/events/2")), eq(false));
    }

    @Test
    void shouldHandleEmptyStats() throws Exception {
        when(statsService.getStats(any(LocalDateTime.class), any(LocalDateTime.class),
                any(), anyBoolean())).thenReturn(List.of());
        String startStr = now.minusDays(1).format(formatter);
        String endStr = now.plusDays(1).format(formatter);
        mockMvc.perform(get("/stats")
                        .param("start", startStr)
                        .param("end", endStr))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}