package ru.practicum.stats.service.request;

import ru.practicum.stats.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.stats.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.stats.dto.request.ParticipationRequestDto;

import java.util.List;

/**
 * Сервис для управления заявками на участие в событиях.
 * Предоставляет операции создания, отмены, получения и изменения статуса заявок.
 */
public interface RequestService {

    /**
     * Создает новую заявку на участие в событии.
     *
     * @param userId   идентификатор пользователя
     * @param eventId  идентификатор события
     * @param clientIp IP-адрес клиента
     * @return созданная заявка
     * @throws ru.practicum.stats.exception.ConflictException если заявка не может быть создана
     * @throws ru.practicum.stats.exception.NotFoundException если пользователь или событие не найдены
     */
    ParticipationRequestDto addParticipationRequest(Long userId, Long eventId, String clientIp);

    /**
     * Возвращает список заявок пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список заявок
     */
    List<ParticipationRequestDto> getUserRequests(Long userId);

    /**
     * Отменяет заявку пользователя.
     *
     * @param userId    идентификатор пользователя
     * @param requestId идентификатор заявки
     * @return отмененная заявка
     * @throws ru.practicum.stats.exception.NotFoundException если заявка не найдена или не принадлежит пользователю
     */
    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    /**
     * Возвращает список заявок на событие (только для инициатора события).
     *
     * @param userId  идентификатор пользователя (должен быть инициатором)
     * @param eventId идентификатор события
     * @return список заявок
     * @throws ru.practicum.stats.exception.NotFoundException если событие не найдено
     * @throws ru.practicum.stats.exception.ValidationException если пользователь не является инициатором
     */
    List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId);

    /**
     * Изменяет статус заявок на событие (подтверждение/отклонение).
     *
     * @param userId  идентификатор пользователя (инициатор события)
     * @param eventId идентификатор события
     * @param request запрос на изменение статуса
     * @return результат обновления с подтвержденными и отклоненными заявками
     * @throws ru.practicum.stats.exception.ConflictException если статус не может быть изменен
     * @throws ru.practicum.stats.exception.NotFoundException если заявки не найдены
     */
    EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                       EventRequestStatusUpdateRequest request);
}