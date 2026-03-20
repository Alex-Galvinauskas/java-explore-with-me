package ru.practicum.stats.service.request;

import ru.practicum.stats.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.stats.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.stats.dto.request.ParticipationRequestDto;

import java.util.List;

public interface RequestService {
    ParticipationRequestDto addParticipationRequest(Long userId, Long eventId, String clientIp); // Добавлен параметр clientIp

    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                       EventRequestStatusUpdateRequest request);
}