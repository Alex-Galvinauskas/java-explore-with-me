package ru.practicum.stats.service.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.dto.request.ParticipationRequestDto;
import ru.practicum.stats.exception.ConflictException;
import ru.practicum.stats.mapper.RequestMapper;
import ru.practicum.stats.model.Event;
import ru.practicum.stats.model.Request;
import ru.practicum.stats.model.User;
import ru.practicum.stats.model.enums.RequestStatus;
import ru.practicum.stats.repository.EventRepository;
import ru.practicum.stats.repository.RequestRepository;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestCreator {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final RequestMapper requestMapper;
    private final RequestValidator requestValidator;

    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        User requester = requestValidator.findUserById(userId);
        Event event = requestValidator.findEventById(eventId);

        Request request = buildRequest(requester, event);

        if (shouldAutoConfirmRequest(event)) {
            autoConfirmRequest(request, event);
        }

        Request saved = requestRepository.save(request);
        log.info("Заявка создана с id={}, статус={}", saved.getId(), saved.getStatus());

        return requestMapper.toDto(saved);
    }

    private Request buildRequest(User requester, Event event) {
        return Request.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(requester)
                .status(RequestStatus.PENDING)
                .build();
    }

    private boolean shouldAutoConfirmRequest(Event event) {
        boolean moderationNotRequired = event.getRequestModeration() != null && !event.getRequestModeration();
        boolean noParticipantLimit = event.getParticipantLimit() == null || event.getParticipantLimit() == 0;
        return moderationNotRequired || noParticipantLimit;
    }

    private void autoConfirmRequest(Request request, Event event) {
        if (event.getParticipantLimit() != null && event.getParticipantLimit() > 0) {
            long confirmedCount = requestRepository.countByEventIdAndStatus(
                    event.getId(), RequestStatus.CONFIRMED);
            if (confirmedCount >= event.getParticipantLimit()) {
                throw new ConflictException("Достигнут лимит участников");
            }
        }
        request.setStatus(RequestStatus.CONFIRMED);
        event.setConfirmedRequests(event.getConfirmedRequests() + 1);
        eventRepository.save(event);
    }
}