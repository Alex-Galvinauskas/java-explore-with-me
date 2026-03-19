package ru.practicum.main.service.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.main.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.main.dto.request.ParticipationRequestDto;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.exception.ValidationException;
import ru.practicum.main.mapper.RequestMapper;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.Request;
import ru.practicum.main.model.User;
import ru.practicum.main.model.enums.EventState;
import ru.practicum.main.model.enums.RequestStatus;
import ru.practicum.main.repository.EventRepository;
import ru.practicum.main.repository.RequestRepository;
import ru.practicum.main.repository.UserRepository;

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
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestMapper requestMapper;

    @Override
    @Transactional
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        log.info("Добавление заявки: userId={}, eventId={}", userId, eventId);

        User requester = userRepository.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new ValidationException("Нельзя создать заявку на неопубликованное событие");
        }

        if (event.getInitiator() != null && event.getInitiator().getId().equals(userId)) {
            throw new ValidationException("Инициатор события не может подать заявку на свое событие");
        }

        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Заявка на это событие уже была отправлена данным пользователем");
        }

        if (event.getParticipantLimit() != null && event.getParticipantLimit() > 0) {
            long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
            if (confirmedCount >= event.getParticipantLimit()) {
                throw new ValidationException("Достигнут лимит участников для данного события");
            }
        }

        Request request = Request.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(requester)
                .status(RequestStatus.PENDING)
                .build();

        if (event.getRequestModeration() != null && !event.getRequestModeration()) {
            request.setStatus(RequestStatus.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }

        Request saved = requestRepository.save(request);
        log.info("Заявка создана с id={}", saved.getId());

        return requestMapper.toDto(saved);
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        log.info("Получение заявок пользователя с id={}", userId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        List<Request> requests = requestRepository.findByRequesterId(userId);
        return requests.stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Отмена заявки: userId={}, requestId={}", userId, requestId);

        Request request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() ->
                        new NotFoundException("Заявка не найдена или не принадлежит пользователю"));

        request.setStatus(RequestStatus.CANCELED);
        Request saved = requestRepository.save(request);

        log.info("Заявка с id={} отменена", saved.getId());
        return requestMapper.toDto(saved);
    }

    @Override
    @Transactional
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        log.info("Получение заявок на событие: userId={}, eventId={}", userId, eventId);

        Event event = findEventById(eventId);
        validateUserIsInitiator(event, userId);

        List<Request> requests = requestRepository.findAllByEventId(eventId);
        return requests.stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest request) {
        log.info("Изменение статусов заявок: userId={}, eventId={}, request={}",
                userId, eventId, request);

        Event event = findEventById(eventId);
        validateUserIsInitiator(event, userId);

        if (event.getParticipantLimit() == null || event.getParticipantLimit() == 0) {
            throw new ConflictException("Для события без лимита участников подтверждение заявок не требуется");
        }

        List<Request> requests = requestRepository.findAllByIdIn(request.getRequestIds());

        if (requests.size() != request.getRequestIds().size()) {
            throw new NotFoundException("Некоторые заявки не найдены");
        }

        for (Request req : requests) {
            if (!req.getEvent().getId().equals(eventId)) {
                throw new ConflictException("Заявка с id=" + req.getId() + " не относится к событию с id=" + eventId);
            }
        }

        for (Request req : requests) {
            if (req.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Заявка с id=" + req.getId() + " имеет статус " + req.getStatus() +
                        ", ожидался PENDING");
            }
        }

        long currentConfirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        int participantLimit = event.getParticipantLimit();

        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        if ("CONFIRMED".equals(request.getStatus())) {
            long availableSlots = participantLimit - currentConfirmed;

            if (availableSlots <= 0) {
                throw new ConflictException("Достигнут лимит участников для данного события");
            }

            if (requests.size() > availableSlots) {
                throw new ConflictException("Недостаточно свободных мест. Доступно: " + availableSlots +
                        ", запрошено: " + requests.size());
            }

            for (Request req : requests) {
                req.setStatus(RequestStatus.CONFIRMED);
                confirmedRequests.add(requestMapper.toDto(req));
            }

            event.setConfirmedRequests(event.getConfirmedRequests() + requests.size());
            eventRepository.save(event);

            if (event.getConfirmedRequests() >= participantLimit) {
                List<Request> pendingRequests = requestRepository.findAllByEventIdAndStatus(eventId,
                        RequestStatus.PENDING);
                for (Request req : pendingRequests) {
                    req.setStatus(RequestStatus.REJECTED);
                    rejectedRequests.add(requestMapper.toDto(req));
                }
                log.info("Лимит участников достигнут, отклонено {} оставшихся заявок", pendingRequests.size());
            }

        } else if ("REJECTED".equals(request.getStatus())) {
            for (Request req : requests) {
                req.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(requestMapper.toDto(req));
            }
        } else {
            throw new ValidationException("Недопустимый статус: " + request.getStatus() +
                    ". Ожидается CONFIRMED или REJECTED");
        }

        requestRepository.saveAll(requests);

        log.info("Статусы заявок обновлены. Подтверждено: {}, Отклонено: {}",
                confirmedRequests.size(), rejectedRequests.size());

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmedRequests)
                .rejectedRequests(rejectedRequests)
                .build();
    }

    private Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));
    }

    private void validateUserIsInitiator(Event event, Long userId) {
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ValidationException("Пользователь не является инициатором события");
        }
    }
}