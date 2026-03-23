package ru.practicum.stats.core.event.builder;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.stats.dto.event.LocationDto;
import ru.practicum.stats.dto.event.UpdateEventAdminRequest;
import ru.practicum.stats.dto.event.UpdateEventUserRequest;
import ru.practicum.stats.model.Event;
import ru.practicum.stats.model.Location;
import ru.practicum.stats.validation.event.EventFieldValidator;

@Component
@RequiredArgsConstructor
public class EventFieldUpdater {

    private final EventFieldValidator fieldValidator;

    public void updateFields(Event event, UpdateEventAdminRequest request) {
        updateFieldsInternal(event,
                request.getAnnotation(),
                request.getDescription(),
                request.getLocation(),
                request.getPaid(),
                request.getParticipantLimit(),
                request.getRequestModeration(),
                request.getTitle());
    }

    public void updateFields(Event event, UpdateEventUserRequest request) {
        updateFieldsInternal(event,
                request.getAnnotation(),
                request.getDescription(),
                request.getLocation(),
                request.getPaid(),
                request.getParticipantLimit(),
                request.getRequestModeration(),
                request.getTitle());
    }

    private void updateFieldsInternal(Event event,
                                      String annotation,
                                      String description,
                                      LocationDto locationDto,
                                      Boolean paid,
                                      Integer participantLimit,
                                      Boolean requestModeration,
                                      String title) {
        updateAnnotation(event, annotation);
        updateDescription(event, description);
        updateLocation(event, locationDto);
        updatePaid(event, paid);
        updateParticipantLimit(event, participantLimit);
        updateRequestModeration(event, requestModeration);
        updateTitle(event, title);
    }

    private void updateAnnotation(Event event, String annotation) {
        if (annotation != null) {
            fieldValidator.validateAnnotation(annotation);
            event.setAnnotation(annotation);
        }
    }

    private void updateDescription(Event event, String description) {
        if (description != null) {
            fieldValidator.validateDescription(description);
            event.setDescription(description);
        }
    }

    private void updateLocation(Event event, LocationDto locationDto) {
        if (locationDto != null) {
            Location location = new Location();
            location.setLat(locationDto.getLat());
            location.setLon(locationDto.getLon());
            event.setLocation(location);
        }
    }

    private void updatePaid(Event event, Boolean paid) {
        if (paid != null) {
            event.setPaid(paid);
        }
    }

    private void updateParticipantLimit(Event event, Integer participantLimit) {
        if (participantLimit != null) {
            fieldValidator.validateParticipantLimit(participantLimit);
            event.setParticipantLimit(participantLimit);
        }
    }

    private void updateRequestModeration(Event event, Boolean requestModeration) {
        if (requestModeration != null) {
            event.setRequestModeration(requestModeration);
        }
    }

    private void updateTitle(Event event, String title) {
        if (title != null) {
            fieldValidator.validateTitle(title);
            event.setTitle(title);
        }
    }
}