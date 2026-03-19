package ru.practicum.main.service.event.builder;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.main.dto.event.LocationDto;
import ru.practicum.main.dto.event.UpdateEventAdminRequest;
import ru.practicum.main.dto.event.UpdateEventUserRequest;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.exception.ValidationException;
import ru.practicum.main.mapper.LocationMapper;
import ru.practicum.main.model.Category;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.enums.EventState;
import ru.practicum.main.repository.CategoryRepository;
import ru.practicum.main.service.event.validation.EventValidator;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class EventUpdater {

    private final CategoryRepository categoryRepository;
    private final LocationMapper locationMapper;
    private final EventValidator validator;

    public void updateCategoryIfNeeded(Event event, Long categoryId) {
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() ->
                            new NotFoundException("Категория с id=" + categoryId + " не найдена"));
            event.setCategory(category);
        }
    }

    public void updateEventFields(Event event, UpdateEventAdminRequest request) {
        updateFields(event,
                request.getAnnotation(),
                request.getDescription(),
                request.getLocation(),
                request.getPaid(),
                request.getParticipantLimit(),
                request.getRequestModeration(),
                request.getTitle());
    }

    public void updateEventFields(Event event, UpdateEventUserRequest request) {
        updateFields(event,
                request.getAnnotation(),
                request.getDescription(),
                request.getLocation(),
                request.getPaid(),
                request.getParticipantLimit(),
                request.getRequestModeration(),
                request.getTitle());
    }

    private void updateFields(Event event,
                              String annotation,
                              String description,
                              LocationDto location,
                              Boolean paid,
                              Integer participantLimit,
                              Boolean requestModeration,
                              String title) {
        if (annotation != null) {
            event.setAnnotation(annotation);
        }
        if (description != null) {
            event.setDescription(description);
        }
        if (location != null) {
            event.setLocation(locationMapper.toEntity(location));
        }
        if (paid != null) {
            event.setPaid(paid);
        }
        if (participantLimit != null) {
            validator.validateParticipantLimit(participantLimit);
            event.setParticipantLimit(participantLimit);
        }
        if (requestModeration != null) {
            event.setRequestModeration(requestModeration);
        }
        if (title != null) {
            event.setTitle(title);
        }
    }

    public EventState handleAdminStateAction(Event event, String stateAction) {
        if (stateAction == null) return event.getState();

        return switch (stateAction) {
            case "PUBLISH_EVENT" -> publishEvent(event);
            case "REJECT_EVENT" -> rejectEvent(event);
            default -> throw new ValidationException("Неизвестное действие: " + stateAction);
        };
    }

    private EventState publishEvent(Event event) {
        if (event.getState() != EventState.PENDING) {
            throw new ConflictException("Нельзя опубликовать событие в статусе " + event.getState());
        }

        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new ConflictException("Дата события должна быть не раньше чем через 1 час от текущего момента");
        }

        event.setPublishedOn(LocalDateTime.now());
        return EventState.PUBLISHED;
    }

    private EventState rejectEvent(Event event) {
        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Нельзя отклонить уже опубликованное событие");
        }
        return EventState.CANCELED;
    }

    public EventState handleUserStateAction(String stateAction) {
        if (stateAction == null) return null;

        return switch (stateAction) {
            case "SEND_TO_REVIEW" -> EventState.PENDING;
            case "CANCEL_REVIEW" -> EventState.CANCELED;
            default -> throw new ValidationException("Неизвестное действие: " + stateAction);
        };
    }
}