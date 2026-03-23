package ru.practicum.stats.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats.dto.event.EventFullDto;
import ru.practicum.stats.dto.event.EventSearchParams;
import ru.practicum.stats.dto.event.UpdateEventAdminRequest;
import ru.practicum.stats.exception.BadRequestException;
import ru.practicum.stats.model.enums.EventState;
import ru.practicum.stats.service.event.EventService;

import java.time.LocalDateTime;
import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/admin/events")
@RequiredArgsConstructor
@Tag(name = "Admin: Events", description = "Управление событиями (административный доступ)")
public class AdminEventController {

    private final EventService eventService;

    @GetMapping
    @Operation(summary = "Получить список событий с фильтрацией")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список событий получен"),
            @ApiResponse(responseCode = "400",
                    description = "Некорректные параметры запроса, неверный формат даты, некорректный диапазон дат")
    })
    public List<EventFullDto> getEvents(
            @Parameter(description = "Список ID пользователей", example = "[1,2,3]")
            @RequestParam(required = false) List<Long> users,
            @Parameter(description = "Список статусов событий")
            @RequestParam(required = false) List<EventState> states,
            @Parameter(description = "Список ID категорий", example = "[1,2,3]")
            @RequestParam(required = false) List<Long> categories,
            @Parameter(description = "Начало диапазона дат (формат: yyyy-MM-dd HH:mm:ss)")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @Parameter(description = "Конец диапазона дат (формат: yyyy-MM-dd HH:mm:ss)")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @Parameter(description = "Индекс первого элемента", example = "0")
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @Parameter(description = "Количество элементов на странице", example = "10")
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new BadRequestException(
                    String.format("Дата начала (%s) не может быть позже даты окончания (%s)",
                            rangeStart, rangeEnd)
            );
        }

        EventSearchParams params = EventSearchParams.builder()
                .users(users)
                .states(states)
                .categories(categories)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .from(from)
                .size(size)
                .build();

        return eventService.getEventsByAdmin(params);
    }

    @PatchMapping("/{eventId}")
    @Operation(summary = "Обновить событие")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Событие обновлено"),
            @ApiResponse(responseCode = "400",
                    description = "Некорректные данные, ошибка валидации, неверный формат запроса"),
            @ApiResponse(responseCode = "404", description = "Событие не найдено"),
            @ApiResponse(responseCode = "409",
                    description = "Конфликт: дата события некорректна, событие уже опубликовано")
    })
    public EventFullDto updateEvent(
            @Parameter(description = "ID события", required = true, example = "1")
            @PathVariable @Positive Long eventId,
            @Valid @RequestBody UpdateEventAdminRequest request) {
        return eventService.updateEventByAdmin(eventId, request);
    }
}