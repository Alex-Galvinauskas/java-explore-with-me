package ru.practicum.stats.validation.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.stats.exception.ConflictException;
import ru.practicum.stats.exception.NotFoundException;
import ru.practicum.stats.exception.ValidationException;
import ru.practicum.stats.model.Event;
import ru.practicum.stats.model.Request;
import ru.practicum.stats.model.User;
import ru.practicum.stats.model.enums.EventState;
import ru.practicum.stats.model.enums.RequestStatus;
import ru.practicum.stats.repository.EventRepository;
import ru.practicum.stats.repository.RequestRepository;
import ru.practicum.stats.repository.UserRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RequestValidator {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;

    public User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }

    public Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));
    }

    public List<Request> findRequestsByIds(List<Long> requestIds) {
        List<Request> requests = requestRepository.findAllByIdIn(requestIds);
        if (requests.size() != requestIds.size()) {
            throw new NotFoundException("Некоторые заявки не найдены");
        }
        return requests;
    }

    public void validateNewRequest(Event event, Long userId) {
        validateEventPublished(event);
        validateNotInitiator(event, userId);
        validateNoExistingRequest(event.getId(), userId);
        validateParticipantLimitNotExceeded(event);
    }

    public void validateEventPublished(Event event) {
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Нельзя создать заявку на неопубликованное событие");
        }
    }

    public void validateNotInitiator(Event event, Long userId) {
        if (event.getInitiator() != null && event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Инициатор события не может подать заявку на свое событие");
        }
    }

    public void validateNoExistingRequest(Long eventId, Long userId) {
        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Заявка на это событие уже была отправлена данным пользователем");
        }
    }

    public void validateParticipantLimitNotExceeded(Event event) {
        if (event.getParticipantLimit() != null && event.getParticipantLimit() > 0) {
            long confirmedCount = requestRepository.countByEventIdAndStatus(
                    event.getId(), RequestStatus.CONFIRMED);
            if (confirmedCount >= event.getParticipantLimit()) {
                throw new ConflictException("Достигнут лимит участников для данного события");
            }
        }
    }

    public void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }

    public void validateUserIsInitiator(Event event, Long userId) {
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ValidationException("Пользователь не является инициатором события");
        }
    }

    public void validateParticipantLimit(Event event) {
        if (event.getParticipantLimit() == null || event.getParticipantLimit() == 0) {
            throw new ConflictException("Для события без лимита участников подтверждение заявок не требуется");
        }
    }

    public void validateRequestsBelongToEvent(List<Request> requests, Long eventId) {
        for (Request req : requests) {
            if (!req.getEvent().getId().equals(eventId)) {
                throw new ConflictException("Заявка с id=" + req.getId() +
                        " не относится к событию с id=" + eventId);
            }
        }
    }

    public void validateRequestsStatusPending(List<Request> requests) {
        for (Request req : requests) {
            if (req.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Заявка с id=" + req.getId() +
                        " имеет статус " + req.getStatus() + ", ожидался PENDING");
            }
        }
    }

    public void validateAvailableSlots(long availableSlots, int requestedCount) {
        if (availableSlots <= 0) {
            throw new ConflictException("Достигнут лимит участников для данного события");
        }
        if (requestedCount > availableSlots) {
            throw new ConflictException("Недостаточно свободных мест. Доступно: " +
                    availableSlots + ", запрошено: " + requestedCount);
        }
    }
}