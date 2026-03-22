package ru.practicum.stats.controller;

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
@Tag(name = "StatsController", description = "API для работы со статистикой посещений")
public class StatsController {

    private final StatsService statsService;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Информация сохранена"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    public EndpointHit hit(
            @Valid @RequestBody
            @Parameter(description = "Данные запроса", required = true)
            EndpointHit hitDto
    ) {
        return statsService.hit(hitDto);
    }

    @GetMapping("/stats")
    public List<ViewStats> getStats(
            @RequestParam() String start,
            @RequestParam() String end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") boolean unique) {

        LocalDateTime startDate;
        LocalDateTime endDate;

        try {
            startDate = LocalDateTime.parse(start, FORMATTER);
            endDate = LocalDateTime.parse(end, FORMATTER);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Неверный формат даты. Ожидается: yyyy-MM-dd HH:mm:ss");
        }

        if (startDate.isAfter(endDate)) {
            throw new BadRequestException(
                    String.format("Дата начала (%s) не может быть позже даты конца (%s)", start, end)
            );
        }

        return statsService.getStats(startDate, endDate, uris, unique);
    }
}