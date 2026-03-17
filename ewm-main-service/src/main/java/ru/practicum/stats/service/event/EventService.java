package ru.practicum.stats.service.event;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.stats.dto.event.EventFullDto;
import ru.practicum.stats.dto.event.EventShortDto;
import ru.practicum.stats.dto.event.NewEventDto;

import java.util.List;

public interface EventService {
    EventFullDto addEvent(Long userId, NewEventDto newEventDto);

    List<EventShortDto> getEventsByUser(Long userId, int from, int size);

    EventFullDto getEvent(Long id, HttpServletRequest request);

    List<EventShortDto> getEvents(EventSearchParams params, HttpServletRequest request);
}