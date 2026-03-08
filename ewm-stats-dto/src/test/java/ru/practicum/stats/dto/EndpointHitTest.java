package ru.practicum.stats.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EndpointHitTest {

    private ObjectMapper objectMapper;
    private Validator validator;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        EndpointHit hit = EndpointHit.builder()
                .id(1L)
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(now)
                .build();

        String json = objectMapper.writeValueAsString(hit);

        assertThat(json).contains("\"app\":\"ewm-main-service\"");
        assertThat(json).contains("\"uri\":\"/events/1\"");
        assertThat(json).contains("\"ip\":\"192.168.1.1\"");
        assertThat(json).contains(now.format(FORMATTER));
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        String json = String.format(
                "{\"app\":\"ewm-main-service\",\"uri\":\"/events/1\",\"ip\":\"192.168.1.1\",\"timestamp\":\"%s\"}",
                now.format(FORMATTER)
        );

        EndpointHit hit = objectMapper.readValue(json, EndpointHit.class);

        assertThat(hit.getApp()).isEqualTo("ewm-main-service");
        assertThat(hit.getUri()).isEqualTo("/events/1");
        assertThat(hit.getIp()).isEqualTo("192.168.1.1");
        assertThat(hit.getTimestamp().format(FORMATTER)).isEqualTo(now.format(FORMATTER));
    }

    @Test
    void shouldFailValidationWhenAppIsBlank() {
        EndpointHit hit = EndpointHit.builder()
                .app("")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(LocalDateTime.now())
                .build();

        Set<ConstraintViolation<EndpointHit>> violations = validator.validate(hit);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("app"));
    }

    @Test
    void shouldFailValidationWhenIpIsInvalid() {
        EndpointHit hit = EndpointHit.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("invalid.ip")
                .timestamp(LocalDateTime.now())
                .build();

        Set<ConstraintViolation<EndpointHit>> violations = validator.validate(hit);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("ip"));
    }

    @Test
    void shouldAcceptLocalhostAsValidIp() {
        EndpointHit hit = EndpointHit.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("localhost")
                .timestamp(LocalDateTime.now())
                .build();

        Set<ConstraintViolation<EndpointHit>> violations = validator.validate(hit);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldAcceptIPv6AsValidIp() {
        EndpointHit hit = EndpointHit.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("2001:0db8:85a3:0000:0000:8a2e:0370:7334")
                .timestamp(LocalDateTime.now())
                .build();

        Set<ConstraintViolation<EndpointHit>> violations = validator.validate(hit);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailValidationWhenTimestampInFuture() {
        EndpointHit hit = EndpointHit.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(LocalDateTime.now().plusDays(1))
                .build();

        Set<ConstraintViolation<EndpointHit>> violations = validator.validate(hit);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("timestamp"));
    }
}