package ru.practicum.main.service.event.builder;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.main.dto.event.NewEventDto;
import ru.practicum.main.mapper.EventMapper;
import ru.practicum.main.mapper.LocationMapper;
import ru.practicum.main.model.Category;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.User;
import ru.practicum.main.model.enums.EventState;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class EventBuilder {

    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;
    private static final long DEFAULT_CONFIRMED_REQUESTS = 0L;
    private static final long DEFAULT_VIEWS = 0L;

    public Event buildFromNewEventDto(NewEventDto dto, User initiator, Category category, LocalDateTime eventDate) {
        Event event = eventMapper.toEntity(dto);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setEventDate(eventDate);
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());

        if (dto.getLocation() != null) {
            event.setLocation(locationMapper.toEntity(dto.getLocation()));
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