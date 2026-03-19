package ru.practicum.stats.controller.privatee;

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
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class PrivateEventController {

    private final EventService eventService;
    private final RequestService requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(@PathVariable Long userId,
                                 @Valid @RequestBody NewEventDto newEventDto) {
        return eventService.addEvent(userId, newEventDto);
    }

    @GetMapping
    public List<EventShortDto> getEvents(@PathVariable Long userId,
                                         @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                         @RequestParam(defaultValue = "10") @Positive int size) {
        return eventService.getEventsByUser(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEvent(@PathVariable Long userId,
                                 @PathVariable Long eventId) {
        return eventService.getEventByUser(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long userId,
                                    @PathVariable Long eventId,
                                    @Valid @RequestBody UpdateEventUserRequest request) {
        return eventService.updateEventByUser(userId, eventId, request);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getEventRequests(@PathVariable Long userId,
                                                          @PathVariable Long eventId) {
        return requestService.getEventRequests(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult changeRequestStatus(@PathVariable Long userId,
                                                              @PathVariable Long eventId,
                                                              @Valid @RequestBody
                                                              EventRequestStatusUpdateRequest request) {
        return requestService.changeRequestStatus(userId, eventId, request);
    }
}