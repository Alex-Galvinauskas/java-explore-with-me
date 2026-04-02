package ru.practicum.stats.service.event;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.dto.event.*;

import java.util.List;

/**
 * Сервис для управления событиями.
 * Предоставляет операции создания, обновления, получения и поиска событий.
 */
public interface EventService {

    /**
     * Добавляет новое событие от имени пользователя.
     *
     * @param userId      идентификатор пользователя-инициатора
     * @param newEventDto данные нового события
     * @return созданное событие
     * @throws ru.practicum.stats.exception.NotFoundException если пользователь или категория не найдены
     * @throws ru.practicum.stats.exception.ValidationException если дата события некорректна
     */
    EventFullDto addEvent(Long userId, NewEventDto newEventDto);

    /**
     * Возвращает список событий пользователя.
     *
     * @param userId идентификатор пользователя
     * @param from   начальная позиция
     * @param size   количество элементов
     * @return список кратких DTO событий
     */
    List<EventShortDto> getEventsByUser(Long userId, int from, int size);

    /**
     * Возвращает событие по идентификатору с учетом просмотров.
     *
     * @param id      идентификатор события
     * @param request HTTP-запрос для сохранения статистики
     * @return полное DTO события
     * @throws ru.practicum.stats.exception.NotFoundException если событие не найдено или не опубликовано
     */
    EventFullDto getEvent(Long id, HttpServletRequest request);

    /**
     * Возвращает список событий для администратора с фильтрацией.
     *
     * @param params параметры поиска
     * @return список полных DTO событий
     */
    List<EventFullDto> getEventsByAdmin(EventSearchParams params);

    /**
     * Возвращает событие пользователя по идентификатору.
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор события
     * @return полное DTO события
     * @throws ru.practicum.stats.exception.NotFoundException если событие не найдено
     * @throws ru.practicum.stats.exception.ValidationException если пользователь не является инициатором
     */
    EventFullDto getEventByUser(Long userId, Long eventId);

    /**
     * Выполняет поиск событий с фильтрацией.
     *
     * @param params  параметры поиска
     * @param request HTTP-запрос для сохранения статистики
     * @return список кратких DTO событий
     */
    List<EventShortDto> getEvents(EventSearchParams params, HttpServletRequest request);

    /**
     * Возвращает событие по идентификатору без учета просмотров.
     *
     * @param id идентификатор события
     * @return полное DTO события
     * @throws ru.practicum.stats.exception.NotFoundException если событие не найдено
     */
    EventFullDto getEventById(Long id);

    /**
     * Обновляет событие администратором.
     *
     * @param eventId идентификатор события
     * @param request данные для обновления
     * @return обновленное событие
     * @throws ru.practicum.stats.exception.NotFoundException если событие или категория не найдены
     * @throws ru.practicum.stats.exception.ConflictException если событие нельзя опубликовать/отклонить
     */
    @Transactional
    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request);

    /**
     * Обновляет событие пользователем.
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор события
     * @param request данные для обновления
     * @return обновленное событие
     * @throws ru.practicum.stats.exception.NotFoundException если событие или пользователь не найдены
     * @throws ru.practicum.stats.exception.ConflictException если событие уже опубликовано
     */
    @Transactional
    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest request);

    /**
     * Удаляет событие администратором.
     *
     * @param eventId идентификатор события
     * @throws ru.practicum.stats.exception.NotFoundException если событие не найдено
     * @throws ru.practicum.stats.exception.ConflictException если событие нельзя удалить
     */
    @Transactional
    void deleteEventByAdmin(Long eventId);
}