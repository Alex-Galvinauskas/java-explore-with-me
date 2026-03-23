package ru.practicum.stats.controller.publicc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats.dto.event.EventFullDto;
import ru.practicum.stats.dto.event.EventSearchParams;
import ru.practicum.stats.dto.event.EventShortDto;
import ru.practicum.stats.service.event.EventService;

import java.time.LocalDateTime;
import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/events")
@RequiredArgsConstructor
@Tag(name = "Public: Events", description = "Публичный API для работы с событиями")
public class PublicEventController {

    private final EventService eventService;

    @GetMapping
    @Operation(summary = "Получить список событий с фильтрацией")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список событий получен"),
            @ApiResponse(responseCode = "400",
                    description = "Некорректные параметры запроса, неверный формат даты, " +
                            "некорректный диапазон дат, неверный тип сортировки")
    })
    public List<EventShortDto> getEvents(
            @Parameter(description = "Текст для поиска в аннотации и описании")
            @RequestParam(required = false) String text,
            @Parameter(description = "Список ID категорий", example = "[1,2,3]")
            @RequestParam(required = false) List<Long> categories,
            @Parameter(description = "Платное событие")
            @RequestParam(required = false) Boolean paid,
            @Parameter(description = "Начало диапазона дат (формат: yyyy-MM-dd HH:mm:ss)")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @Parameter(description = "Конец диапазона дат (формат: yyyy-MM-dd HH:mm:ss)")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @Parameter(description = "Только доступные события (не превышен лимит участников)")
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @Parameter(description = "Сортировка: EVENT_DATE или VIEWS", example = "EVENT_DATE")
            @RequestParam(defaultValue = "EVENT_DATE") String sort,
            @Parameter(description = "Индекс первого элемента", example = "0")
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @Parameter(description = "Количество элементов на странице", example = "10")
            @RequestParam(defaultValue = "10") @Positive Integer size,
            HttpServletRequest request) {

        EventSearchParams params = EventSearchParams.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .sort(sort)
                .from(from)
                .size(size)
                .build();

        return eventService.getEvents(params, request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить событие по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Событие найдено"),
            @ApiResponse(responseCode = "400", description = "Некорректный ID события"),
            @ApiResponse(responseCode = "404", description = "Событие не найдено или не опубликовано")
    })
    public EventFullDto getEvent(
            @Parameter(description = "ID события", required = true, example = "1")
            @PathVariable @Positive Long id,
            HttpServletRequest request) {
        return eventService.getEvent(id, request);
    }
}