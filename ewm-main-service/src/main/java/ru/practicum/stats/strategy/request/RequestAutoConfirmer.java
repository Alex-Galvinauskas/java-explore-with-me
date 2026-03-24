package ru.practicum.stats.strategy.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.stats.exception.ConflictException;
import ru.practicum.stats.model.Event;
import ru.practicum.stats.model.Request;
import ru.practicum.stats.model.enums.RequestStatus;
import ru.practicum.stats.repository.EventRepository;
import ru.practicum.stats.repository.RequestRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestAutoConfirmer {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;

    public boolean shouldAutoConfirm(Event event) {
        boolean moderationNotRequired = event.getRequestModeration() != null && !event.getRequestModeration();
        boolean noParticipantLimit = event.getParticipantLimit() == null || event.getParticipantLimit() == 0;
        return moderationNotRequired || noParticipantLimit;
    }

    public void autoConfirm(Request request, Event event) {
        if (hasParticipantLimit(event)) {
            validateAvailableSlots(event);
        }

        request.setStatus(RequestStatus.CONFIRMED);
        incrementEventConfirmedCount(event);
        eventRepository.save(event);
    }

    private boolean hasParticipantLimit(Event event) {
        return event.getParticipantLimit() != null && event.getParticipantLimit() > 0;
    }

    private void validateAvailableSlots(Event event) {
        long confirmedCount = requestRepository.countByEventIdAndStatus(
                event.getId(), RequestStatus.CONFIRMED);
        if (confirmedCount >= event.getParticipantLimit()) {
            throw new ConflictException("Достигнут лимит участников");
        }
    }

    private void incrementEventConfirmedCount(Event event) {
        event.setConfirmedRequests(event.getConfirmedRequests() + 1);
    }
}