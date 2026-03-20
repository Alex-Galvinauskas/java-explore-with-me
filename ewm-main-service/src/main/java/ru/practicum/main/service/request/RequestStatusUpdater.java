package ru.practicum.main.service.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.main.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.main.dto.request.ParticipationRequestDto;
import ru.practicum.main.mapper.RequestMapper;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.Request;
import ru.practicum.main.model.enums.RequestStatus;
import ru.practicum.main.repository.EventRepository;
import ru.practicum.main.repository.RequestRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestStatusUpdater {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final RequestMapper requestMapper;
    private final RequestValidator requestValidator;

    @Transactional
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest request) {
        Event event = requestValidator.findEventById(eventId);
        requestValidator.validateUserIsInitiator(event, userId);
        requestValidator.validateParticipantLimit(event);

        List<Request> requests = requestValidator.validateAndGetRequests(request.getRequestIds(), eventId);

        if ("CONFIRMED".equals(request.getStatus())) {
            return confirmRequests(event, requests);
        } else {
            return rejectRequests(requests);
        }
    }

    private EventRequestStatusUpdateResult confirmRequests(Event event, List<Request> requests) {
        long currentConfirmed = requestRepository.countByEventIdAndStatus(event.getId(),
                RequestStatus.CONFIRMED);
        long availableSlots = event.getParticipantLimit() - currentConfirmed;

        requestValidator.validateAvailableSlots(availableSlots, requests.size());

        List<ParticipationRequestDto> confirmedRequests = confirmSelectedRequests(requests);

        event.setConfirmedRequests(event.getConfirmedRequests() + requests.size());
        eventRepository.save(event);

        List<ParticipationRequestDto> rejectedRequests = rejectRemainingRequestsIfLimitReached(event);

        requestRepository.saveAll(requests);

        log.info("Подтверждено заявок: {}, Отклонено: {}",
                confirmedRequests.size(), rejectedRequests.size());

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmedRequests)
                .rejectedRequests(rejectedRequests)
                .build();
    }

    private List<ParticipationRequestDto> confirmSelectedRequests(List<Request> requests) {
        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        for (Request req : requests) {
            req.setStatus(RequestStatus.CONFIRMED);
            confirmedRequests.add(requestMapper.toDto(req));
        }
        return confirmedRequests;
    }

    private List<ParticipationRequestDto> rejectRemainingRequestsIfLimitReached(Event event) {
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        if (event.getConfirmedRequests() >= event.getParticipantLimit()) {
            List<Request> pendingRequests = requestRepository.findAllByEventIdAndStatus(
                    event.getId(), RequestStatus.PENDING);

            for (Request req : pendingRequests) {
                req.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(requestMapper.toDto(req));
            }

            log.info("Лимит участников достигнут, отклонено {} оставшихся заявок", pendingRequests.size());
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