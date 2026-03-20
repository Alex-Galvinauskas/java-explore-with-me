package ru.practicum.main.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.dto.EndpointHit;
import ru.practicum.main.dto.ViewStats;
import ru.practicum.main.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping
@Tag(name = "StatsController", description = "API для работы со статистикой посещений")
public class StatsController {

    private final StatsService statsService;

    @PostMapping("/hit")
    @Operation(summary = "Сохранение информации о том, что к эндпоинту был запрос",
            description = "Сохранение информации о том, что на uri конкретного сервиса был отправлен запрос пользователем")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Информация сохранена"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    public ResponseEntity<Void> hit(
            @Valid @RequestBody
            @Parameter(description = "Данные запроса", required = true)
            EndpointHit hitDto
    ) {
        statsService.hit(hitDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/stats")
    @Operation(summary = "Получение статистики по посещениям",
            description = "Возвращает статистику посещений за указанный период с возможностью фильтрации")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статистика собрана",
                    content = @Content(schema = @Schema(implementation = ViewStats.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    public ResponseEntity<List<ViewStats>> getStats(
            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            @Parameter(description = "Дата и время начала диапазона (формат: yyyy-MM-dd HH:mm:ss)",
                    example = "2022-09-06 10:00:00", required = true)
            LocalDateTime start,

            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            @Parameter(description = "Дата и время конца диапазона (формат: yyyy-MM-dd HH:mm:ss)",
                    example = "2022-09-06 12:00:00", required = true)
            LocalDateTime end,

            @RequestParam(required = false)
            @Parameter(description = "Список URI для фильтрации", example = "/events/1")
            List<String> uris,

            @RequestParam(defaultValue = "false")
            @Parameter(description = "Учитывать только уникальные IP", example = "false")
            boolean unique
    ) {

        List<ViewStats> stats = statsService.getStats(start, end, uris, unique);

        return ResponseEntity.ok(stats);
    }
}