package ru.practicum.stats.controller.privatee;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats.dto.event.EventFullDto;
import ru.practicum.stats.dto.event.EventShortDto;
import ru.practicum.stats.dto.event.NewEventDto;
import ru.practicum.stats.dto.event.UpdateEventUserRequest;
import ru.practicum.stats.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.stats.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.stats.dto.request.ParticipationRequestDto;
import ru.practicum.stats.service.event.EventService;
import ru.practicum.stats.service.request.RequestService;

import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/users/{userId}/events")
@RequiredArgsConstructor
@Tag(name = "Private: Events", description = "Управление событиями текущего пользователя")
public class PrivateEventController {

    private final EventService eventService;
    private final RequestService requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Добавить новое событие")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Событие создано"),
            @ApiResponse(responseCode = "400",
                    description = "Некорректные данные, ошибка валидации," +
                            " неверный формат даты, дата события раньше текущего времени"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден, категория не найдена"),
            @ApiResponse(responseCode = "409", description = "Конфликт: дата события некорректна")
    })
    public EventFullDto addEvent(
            @Parameter(description = "ID пользователя", required = true, example = "1")
            @PathVariable @Positive Long userId,
            @Valid @RequestBody NewEventDto newEventDto) {
        return eventService.addEvent(userId, newEventDto);
    }

    @GetMapping
    @Operation(summary = "Получить все события текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список событий получен"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public List<EventShortDto> getEvents(
            @Parameter(description = "ID пользователя", required = true, example = "1")
            @PathVariable @Positive Long userId,
            @Parameter(description = "Индекс первого элемента", example = "0")
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @Parameter(description = "Количество элементов на странице", example = "10")
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        return eventService.getEventsByUser(userId, from, size);
    }

    @GetMapping("/{eventId}")
    @Operation(summary = "Получить полную информацию о событии текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Событие найдено"),
            @ApiResponse(responseCode = "400", description = "Некорректный ID события"),
            @ApiResponse(responseCode = "404",
                    description = "Пользователь не найден, событие не найдено, событие не принадлежит пользователю")
    })
    public EventFullDto getEvent(
            @Parameter(description = "ID пользователя", required = true, example = "1")
            @PathVariable @Positive Long userId,
            @Parameter(description = "ID события", required = true, example = "1")
            @PathVariable @Positive Long eventId) {
        return eventService.getEventByUser(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    @Operation(summary = "Обновить событие")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Событие обновлено"),
            @ApiResponse(responseCode = "400",
                    description = "Некорректные данные, ошибка валидации, неверный формат даты"),
            @ApiResponse(responseCode = "404",
                    description = "Пользователь не найден, событие не найдено, событие не принадлежит пользователю"),
            @ApiResponse(responseCode = "409",
                    description = "Конфликт: событие нельзя изменить (уже опубликовано), дата события некорректна")
    })
    public EventFullDto updateEvent(
            @Parameter(description = "ID пользователя", required = true, example = "1")
            @PathVariable @Positive Long userId,
            @Parameter(description = "ID события", required = true, example = "1")
            @PathVariable @Positive Long eventId,
            @Valid @RequestBody UpdateEventUserRequest request) {
        return eventService.updateEventByUser(userId, eventId, request);
    }

    @GetMapping("/{eventId}/requests")
    @Operation(summary = "Получить список запросов на участие в событии текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список запросов получен"),
            @ApiResponse(responseCode = "400", description = "Некорректный ID события"),
            @ApiResponse(responseCode = "404",
                    description = "Пользователь не найден, событие не найдено, событие не принадлежит пользователю")
    })
    public List<ParticipationRequestDto> getEventRequests(
            @Parameter(description = "ID пользователя", required = true, example = "1")
            @PathVariable @Positive Long userId,
            @Parameter(description = "ID события", required = true, example = "1")
            @PathVariable @Positive Long eventId) {
        return requestService.getEventRequests(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    @Operation(summary = "Изменить статус запросов на участие в событии")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статусы запросов обновлены"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные, неверный статус запроса"),
            @ApiResponse(responseCode = "404",
                    description = "Пользователь не найден, событие не найдено, запросы не найдены"),
            @ApiResponse(responseCode = "409",
                    description = "Конфликт: лимит участников превышен, нельзя отклонить подтвержденные запросы")
    })
    public EventRequestStatusUpdateResult changeRequestStatus(
            @Parameter(description = "ID пользователя", required = true, example = "1")
            @PathVariable @Positive Long userId,
            @Parameter(description = "ID события", required = true, example = "1")
            @PathVariable @Positive Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequest request) {
        return requestService.changeRequestStatus(userId, eventId, request);
    }
}