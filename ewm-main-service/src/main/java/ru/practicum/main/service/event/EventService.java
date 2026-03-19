package ru.practicum.main.service.event;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.event.*;

import java.util.List;

public interface EventService {
    EventFullDto addEvent(Long userId, NewEventDto newEventDto);

    List<EventShortDto> getEventsByUser(Long userId, int from, int size);

    EventFullDto getEvent(Long id, HttpServletRequest request);

    List<EventFullDto> getEventsByAdmin(EventSearchParams params);

    EventFullDto getEventByUser(Long userId, Long eventId);

    List<EventShortDto> getEvents(EventSearchParams params, HttpServletRequest request);

    EventFullDto getEventById(Long id);

    @Transactional
    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request);

    @Transactional
    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest request);
}