package ru.practicum.stats.service.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.dto.request.ParticipationRequestDto;
import ru.practicum.stats.exception.NotFoundException;
import ru.practicum.stats.mapper.RequestMapper;
import ru.practicum.stats.model.Event;
import ru.practicum.stats.model.Request;
import ru.practicum.stats.model.enums.RequestStatus;
import ru.practicum.stats.repository.EventRepository;
import ru.practicum.stats.repository.RequestRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestCanceller {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final RequestMapper requestMapper;
    private final RequestValidator requestValidator;

    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        Request request = findUserRequest(requestId, userId);

        if (request.getStatus() == RequestStatus.CONFIRMED) {
            decreaseConfirmedRequests(request.getEvent());
        }

        request.setStatus(RequestStatus.CANCELED);
        Request saved = requestRepository.save(request);

        log.info("Заявка с id={} отменена", saved.getId());
        return requestMapper.toDto(saved);
    }

    private Request findUserRequest(Long requestId, Long userId) {
        return requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() ->
                        new NotFoundException("Заявка не найдена или не принадлежит пользователю"));
    }

    private void decreaseConfirmedRequests(Event event) {
        event.setConfirmedRequests(event.getConfirmedRequests() - 1);
        eventRepository.save(event);
    }
}