package ru.practicum.main.service.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.main.dto.request.ParticipationRequestDto;
import ru.practicum.main.mapper.RequestMapper;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.Request;
import ru.practicum.main.repository.RequestRepository;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RequestFetcher {

    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final RequestValidator requestValidator;

    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        requestValidator.validateUserExists(userId);

        List<Request> requests = requestRepository.findByRequesterId(userId);
        return requests.stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        Event event = requestValidator.findEventById(eventId);
        requestValidator.validateUserIsInitiator(event, userId);

        List<Request> requests = requestRepository.findAllByEventId(eventId);
        return requests.stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }
}