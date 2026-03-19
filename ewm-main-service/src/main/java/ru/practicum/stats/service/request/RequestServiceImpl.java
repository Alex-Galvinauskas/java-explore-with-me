package ru.practicum.stats.service.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.stats.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.stats.dto.request.ParticipationRequestDto;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestCreator requestCreator;
    private final RequestCanceller requestCanceller;
    private final RequestStatusUpdater requestStatusUpdater;
    private final RequestFetcher requestFetcher;
    private final RequestValidator requestValidator;
    private final ViewStatsIncrementor viewStatsIncrementor;

    @Override
    @Transactional
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId, String clientIp) {
        log.info("Добавление заявки: userId={}, eventId={}, clientIp={}", userId, eventId, clientIp);

        requestValidator.validateNewRequest(userId, eventId);

        ParticipationRequestDto createdRequest = requestCreator.createRequest(userId, eventId);

        viewStatsIncrementor.incrementEventViews(eventId, clientIp);

        return createdRequest;
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        return requestFetcher.getUserRequests(userId);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        return requestCanceller.cancelRequest(userId, requestId);
    }

    @Override
    @Transactional
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        return requestFetcher.getEventRequests(userId, eventId);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest request) {
        return requestStatusUpdater.changeRequestStatus(userId, eventId, request);
    }
}