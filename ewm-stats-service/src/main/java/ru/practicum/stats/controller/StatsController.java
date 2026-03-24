package ru.practicum.stats.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.stats.exception.BadRequestException;
import ru.practicum.stats.service.StatsService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping
@Tag(name = "Stats", description = "API для работы со статистикой посещений")
public class StatsController {

    private final StatsService statsService;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @PostMapping("/hit")
    @Operation(summary = "Сохранить информацию о посещении")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Информация сохранена"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "409", description = "Конфликт данных"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<EndpointHit> hit(@Valid @RequestBody EndpointHit hitDto) {
        EndpointHit hit = statsService.hit(hitDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(hit);
    }

    @GetMapping("/stats")
    @Operation(summary = "Получить статистику посещений")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статистика получена"),
            @ApiResponse(responseCode = "400",
                    description = "Неверный формат даты (yyyy-MM-dd HH:mm:ss) или другие ошибки валидации"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<List<ViewStats>> getStats(
            @Parameter(description = "Начало периода (формат: yyyy-MM-dd HH:mm:ss)", required = true)
            @RequestParam String start,

            @Parameter(description = "Окончание периода (формат: yyyy-MM-dd HH:mm:ss)", required = true)
            @RequestParam String end,

            @Parameter(description = "Список URI для фильтрации")
            @RequestParam(required = false) List<String> uris,

            @Parameter(description = "Учитывать только уникальные посещения")
            @RequestParam(defaultValue = "false") boolean unique) {

        LocalDateTime startDate;
        LocalDateTime endDate;

        try {
            startDate = LocalDateTime.parse(start, FORMATTER);
            endDate = LocalDateTime.parse(end, FORMATTER);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Неверный формат даты. Ожидается: yyyy-MM-dd HH:mm:ss");
        }

        List<ViewStats> stats = statsService.getStats(startDate, endDate, uris, unique);
        return ResponseEntity.ok(stats);
    }
}