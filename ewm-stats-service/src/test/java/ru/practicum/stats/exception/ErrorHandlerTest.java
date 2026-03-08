package ru.practicum.stats.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.stats.controller.StatsController;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.service.StatsService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ErrorHandlerTest {

    private MockMvc mockMvc;

    @Mock
    private StatsService statsService;

    @InjectMocks
    private StatsController statsController;

    private ObjectMapper objectMapper;
    private DateTimeFormatter formatter;
    private LocalDateTime now;
    private EndpointHit validHit;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        now = LocalDateTime.now();

        validHit = EndpointHit.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(now)
                .build();

        mockMvc = MockMvcBuilders.standaloneSetup(statsController)
                .setControllerAdvice(new ErrorHandler())
                .build();
    }

    @Test
    void shouldReturn400WhenEndpointHitInvalid() throws Exception {
        // Given
        EndpointHit invalidHit = EndpointHit.builder()
                .app("") // Пустое поле
                .uri("/events/1")
                .ip("invalid-ip")
                .timestamp(now)
                .build();

        // When/Then
        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidHit)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.reason").value("Ошибка валидации"))
                .andExpect(jsonPath("$.message").value("Переданы некорректные данные"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturn400WhenMissingStartParam() throws Exception {
        // When/Then
        mockMvc.perform(get("/stats")
                        .param("end", now.format(formatter)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.reason").value("Отсутствует обязательный параметр"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn400WhenInvalidDateFormat() throws Exception {
        // When/Then
        mockMvc.perform(get("/stats")
                        .param("start", "invalid-date")
                        .param("end", now.format(formatter)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.reason").value("Ошибка типа данных"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn400WhenInvalidJson() throws Exception {
        // When/Then
        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.reason").value("Ошибка формата запроса"))
                .andExpect(jsonPath("$.message").value("Тело запроса содержит некорректные данные"));
    }

    @Test
    void shouldReturn400WhenIllegalArgument() throws Exception {
        // Given
        doThrow(new IllegalArgumentException("Некорректный диапазон дат"))
                .when(statsService).getStats(any(), any(), any(), anyBoolean());

        // When/Then
        mockMvc.perform(get("/stats")
                        .param("start", now.minusDays(1).format(formatter))
                        .param("end", now.plusDays(1).format(formatter)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.reason").value("Некорректный аргумент"))
                .andExpect(jsonPath("$.message").value("Некорректный диапазон дат"));
    }

    @Test
    void shouldReturn400WhenBadRequestException() throws Exception {
        // Given
        doThrow(new BadRequestException("Некорректные параметры фильтрации"))
                .when(statsService).getStats(any(), any(), any(), anyBoolean());

        // When/Then
        mockMvc.perform(get("/stats")
                        .param("start", now.minusDays(1).format(formatter))
                        .param("end", now.plusDays(1).format(formatter)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.reason").value("Некорректный запрос"))
                .andExpect(jsonPath("$.message").value("Некорректные параметры фильтрации"));
    }

    @Test
    void shouldReturn400WhenStatsValidationException() throws Exception {
        // Given
        doThrow(new StatsValidationException("Дата начала не может быть позже даты конца",
                "Некорректный диапазон дат"))
                .when(statsService).getStats(any(), any(), any(), anyBoolean());

        // When/Then
        mockMvc.perform(get("/stats")
                        .param("start", now.plusDays(1).format(formatter))
                        .param("end", now.minusDays(1).format(formatter)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.reason").value("Некорректный диапазон дат"))
                .andExpect(jsonPath("$.message").value("Дата начала не может быть позже даты конца"));
    }

    @Test
    void shouldReturn500WhenUnexpectedException() throws Exception {
        // Given
        doThrow(new RuntimeException("Неожиданная ошибка базы данных"))
                .when(statsService).getStats(any(), any(), any(), anyBoolean());

        // When/Then
        mockMvc.perform(get("/stats")
                        .param("start", now.minusDays(1).format(formatter))
                        .param("end", now.plusDays(1).format(formatter)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.reason").value("Внутренняя ошибка сервера"))
                .andExpect(jsonPath("$.message").value("Произошла непредвиденная ошибка"));
    }

    @Test
    void shouldReturn400WhenMethodArgumentTypeMismatch() throws Exception {
        // When/Then
        mockMvc.perform(get("/stats")
                        .param("start", now.minusDays(1).format(formatter))
                        .param("end", now.plusDays(1).format(formatter))
                        .param("unique", "not-a-boolean"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.reason").value("Ошибка типа данных"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldHandleDataIntegrityViolation() throws Exception {
        // Given
        doThrow(new DataIntegrityViolationException("Duplicate entry"))
                .when(statsService).hit(any(EndpointHit.class));

        // When/Then
        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validHit)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("CONFLICT"))
                .andExpect(jsonPath("$.reason").value("Нарушение целостности данных"))
                .andExpect(jsonPath("$.message").value("Операция нарушает целостность данных"));
    }
}