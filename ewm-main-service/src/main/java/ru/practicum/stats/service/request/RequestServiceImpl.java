package ru.practicum.stats.service.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.stats.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.stats.dto.request.ParticipationRequestDto;
import ru.practicum.stats.exception.NotFoundException;
import ru.practicum.stats.mapper.RequestMapper;
import ru.practicum.stats.model.Event;
import ru.practicum.stats.model.Request;
import ru.practicum.stats.model.User;
import ru.practicum.stats.model.enums.RequestStatus;
import ru.practicum.stats.repository.EventRepository;
import ru.practicum.stats.repository.RequestRepository;
import ru.practicum.stats.repository.UserRepository;
import ru.practicum.stats.statistics.request.ViewStatsIncrementor;
import ru.practicum.stats.validation.request.RequestValidator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestMapper requestMapper;
    private final RequestValidator requestValidator;
    private final ViewStatsIncrementor viewStatsIncrementor;

    @Override
    @Transactional
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId, String clientIp) {
        log.info("Добавление заявки: userId={}, eventId={}", userId, eventId);

        User requester = requestValidator.findUserById(userId);
        Event event = requestValidator.findEventById(eventId);

        requestValidator.validateNewRequest(event, userId);

        Request request = buildRequest(requester, event);

        if (shouldAutoConfirm(event)) {
            autoConfirm(request, event);
        }

        Request saved = requestRepository.save(request);

        viewStatsIncrementor.incrementEventViews(eventId, clientIp);

        log.info("Заявка создана: id={}, status={}", saved.getId(), saved.getStatus());
        return requestMapper.toDto(saved);
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        requestValidator.validateUserExists(userId);
        return requestRepository.findByRequesterId(userId).stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        Request request = findUserRequestOrThrow(requestId, userId);

        if (request.getStatus() == RequestStatus.CONFIRMED) {
            Event event = request.getEvent();
            event.setConfirmedRequests(event.getConfirmedRequests() - 1);
            eventRepository.save(event);
        }

        request.setStatus(RequestStatus.CANCELED);
        Request saved = requestRepository.save(request);

        log.info("Заявка отменена: id={}", saved.getId());
        return requestMapper.toDto(saved);
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        Event event = requestValidator.findEventById(eventId);
        requestValidator.validateUserIsInitiator(event, userId);
        return requestRepository.findAllByEventId(eventId).stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest request) {
        Event event = requestValidator.findEventById(eventId);
        requestValidator.validateUserIsInitiator(event, userId);
        requestValidator.validateParticipantLimit(event);

        List<Request> requests = requestValidator.findRequestsByIds(request.getRequestIds());
        requestValidator.validateRequestsBelongToEvent(requests, eventId);
        requestValidator.validateRequestsStatusPending(requests);

        if ("CONFIRMED".equals(request.getStatus())) {
            return confirmRequests(event, requests);
        } else {
            return rejectRequests(requests);
        }
    }

    private Request findUserRequestOrThrow(Long requestId, Long userId) {
        return requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() ->
                        new NotFoundException("Заявка не найдена или не принадлежит пользователю"));
    }

    private Request buildRequest(User requester, Event event) {
        return Request.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(requester)
                .status(RequestStatus.PENDING)
                .build();
    }

    private boolean shouldAutoConfirm(Event event) {
        return event.getParticipantLimit() == 0 || !event.getRequestModeration();
    }

    private void autoConfirm(Request request, Event event) {
        request.setStatus(RequestStatus.CONFIRMED);
        event.setConfirmedRequests(event.getConfirmedRequests() + 1);
        eventRepository.save(event);
    }

    private EventRequestStatusUpdateResult confirmRequests(Event event, List<Request> requests) {
        long availableSlots = event.getParticipantLimit() - requestRepository.countByEventIdAndStatus(
                event.getId(), RequestStatus.CONFIRMED);

        requestValidator.validateAvailableSlots(availableSlots, requests.size());

        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        for (Request req : requests) {
            req.setStatus(RequestStatus.CONFIRMED);
            confirmedRequests.add(requestMapper.toDto(req));
        }

        event.setConfirmedRequests(event.getConfirmedRequests() + requests.size());
        eventRepository.save(event);

        List<ParticipationRequestDto> rejectedRequests = rejectRemainingPendingRequests(event);

        requestRepository.saveAll(requests);

        log.info("Подтверждено заявок: {}, Отклонено: {}", confirmedRequests.size(), rejectedRequests.size());

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmedRequests)
                .rejectedRequests(rejectedRequests)
                .build();
    }

    private List<ParticipationRequestDto> rejectRemainingPendingRequests(Event event) {
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        if (event.getConfirmedRequests() >= event.getParticipantLimit()) {
            List<Request> pendingRequests = requestRepository.findAllByEventIdAndStatus(
                    event.getId(), RequestStatus.PENDING);

            for (Request req : pendingRequests) {
                req.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(requestMapper.toDto(req));
            }

            requestRepository.saveAll(pendingRequests);
        }

        return rejectedRequests;
    }

    private EventRequestStatusUpdateResult rejectRequests(List<Request> requests) {
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        for (Request req : requests) {
            req.setStatus(RequestStatus.REJECTED);
            rejectedRequests.add(requestMapper.toDto(req));
        }

        requestRepository.saveAll(requests);

        log.info("Отклонено заявок: {}", rejectedRequests.size());

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(new ArrayList<>())
                .rejectedRequests(rejectedRequests)
                .build();
    }
}