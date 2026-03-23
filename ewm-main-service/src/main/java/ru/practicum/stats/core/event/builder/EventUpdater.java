package ru.practicum.stats.core.event.builder;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.stats.dto.event.UpdateEventAdminRequest;
import ru.practicum.stats.dto.event.UpdateEventUserRequest;
import ru.practicum.stats.exception.ConflictException;
import ru.practicum.stats.exception.NotFoundException;
import ru.practicum.stats.exception.ValidationException;
import ru.practicum.stats.model.Category;
import ru.practicum.stats.model.Event;
import ru.practicum.stats.model.enums.EventState;
import ru.practicum.stats.repository.CategoryRepository;
import ru.practicum.stats.validation.event.EventValidator;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class EventUpdater {

    private final CategoryRepository categoryRepository;
    private final EventValidator validator;
    private final EventFieldUpdater fieldUpdater;

    public void updateCategoryIfNeeded(Event event, Long categoryId) {
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() ->
                            new NotFoundException("Категория с id=" + categoryId + " не найдена"));
            event.setCategory(category);
        }
    }

    public void updateEventFields(Event event, UpdateEventAdminRequest request) {
        fieldUpdater.updateFields(event, request);
    }

    public void updateEventFields(Event event, UpdateEventUserRequest request) {
        fieldUpdater.updateFields(event, request);
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

        validator.validateEventDateNotInPast(event.getEventDate(), 1);

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