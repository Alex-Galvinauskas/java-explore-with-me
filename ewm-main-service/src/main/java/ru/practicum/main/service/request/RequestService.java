package ru.practicum.main.service.request;

import ru.practicum.main.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.main.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.main.dto.request.ParticipationRequestDto;

import java.util.List;

public interface RequestService {
    ParticipationRequestDto addParticipationRequest(Long userId, Long eventId);

    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                       EventRequestStatusUpdateRequest request);
}