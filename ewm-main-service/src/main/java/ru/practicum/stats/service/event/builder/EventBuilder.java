package ru.practicum.stats.service.event.builder;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.stats.dto.event.NewEventDto;
import ru.practicum.stats.mapper.EventMapper;
import ru.practicum.stats.model.Category;
import ru.practicum.stats.model.Event;
import ru.practicum.stats.model.User;
import ru.practicum.stats.model.enums.EventState;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class EventBuilder {

    private final EventMapper eventMapper;
    private static final long DEFAULT_CONFIRMED_REQUESTS = 0L;
    private static final long DEFAULT_VIEWS = 0L;

    public Event buildFromNewEventDto(NewEventDto dto, User initiator, Category category) {
        Event event = eventMapper.toEntity(dto);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());

        // Устанавливаем координаты напрямую
        if (dto.getLocation() != null) {
            event.setLat(dto.getLocation().getLat());
            event.setLon(dto.getLocation().getLon());
        }

        setDefaultFields(event);

        return event;
    }

    private void setDefaultFields(Event event) {
        if (event.getPaid() == null) event.setPaid(false);
        if (event.getParticipantLimit() == null) event.setParticipantLimit(0);
        if (event.getRequestModeration() == null) event.setRequestModeration(true);
        event.setConfirmedRequests(DEFAULT_CONFIRMED_REQUESTS);
        event.setViews(DEFAULT_VIEWS);
    }
}