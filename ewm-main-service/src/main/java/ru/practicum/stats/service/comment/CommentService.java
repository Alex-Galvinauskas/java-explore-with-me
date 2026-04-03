package ru.practicum.stats.service.comment;

import ru.practicum.stats.dto.comment.CommentDto;
import ru.practicum.stats.dto.comment.NewCommentDto;
import ru.practicum.stats.dto.comment.UpdateCommentDto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис для управления комментариями к событиям.
 */
public interface CommentService {

    /**
     * Создаёт новый комментарий к событию от пользователя.
     *
     * @param userId  идентификатор автора комментария
     * @param eventId идентификатор события, к которому оставляется комментарий
     * @param dto     данные нового комментария
     * @return DTO созданного комментария
     */
    CommentDto createComment(Long userId, Long eventId, NewCommentDto dto);

    /**
     * Возвращает комментарий по его идентификатору.
     * Доступны только опубликованные комментарии.
     *
     * @param commentId идентификатор комментария
     * @return DTO комментария
     * @throws ru.practicum.stats.exception.CommentNotFoundException если комментарий не найден или не опубликован
     */
    CommentDto getCommentById(Long commentId);

    /**
     * Обновляет комментарий автором.
     * Можно обновить только комментарии в статусе PENDING или REJECTED.
     *
     * @param userId    идентификатор автора
     * @param commentId идентификатор комментария
     * @param dto       обновлённые данные комментария
     * @return DTO обновлённого комментария
     */
    CommentDto updateCommentByAuthor(Long userId, Long commentId, UpdateCommentDto dto);

    /**
     * Мягкое удаление комментария автором.
     * Можно удалить только комментарии в статусе PENDING или REJECTED.
     *
     * @param userId    идентификатор автора
     * @param commentId идентификатор комментария
     */
    void deleteCommentByAuthor(Long userId, Long commentId);

    /**
     * Полное удаление комментария администратором.
     * Комментарий полностью удаляется из базы данных.
     *
     * @param commentId идентификатор комментария
     */
    void deleteCommentByAdmin(Long commentId);

    /**
     * Публикует комментарий.
     * Можно опубликовать только комментарии в статусе PENDING.
     *
     * @param commentId идентификатор комментария
     * @return DTO опубликованного комментария
     */
    CommentDto publishComment(Long commentId);

    /**
     * Отклоняет комментарий.
     * Можно отклонить только комментарии в статусе PENDING.
     *
     * @param commentId идентификатор комментария
     * @return DTO отклонённого комментария
     */
    CommentDto rejectComment(Long commentId);

    /**
     * Возвращает все опубликованные комментарии к событию.
     *
     * @param eventId идентификатор события
     * @param from    начальный индекс для пагинации
     * @param size    количество записей на странице
     * @return список DTO комментариев
     */
    List<CommentDto> getCommentsByEventId(Long eventId, Integer from, Integer size);

    /**
     * Возвращает все комментарии пользователя (все статусы).
     *
     * @param userId идентификатор пользователя
     * @param from   начальный индекс для пагинации
     * @param size   количество записей на странице
     * @return список DTO комментариев
     */
    List<CommentDto> getCommentsByUserId(Long userId, Integer from, Integer size);

    /**
     * Поиск комментариев с фильтрацией по тексту, статусу и диапазону дат.
     *
     * @param text       текст для поиска в содержимом комментария
     * @param status     статус комментария (PENDING, PUBLISHED, REJECTED, DELETED)
     * @param rangeStart начало диапазона дат создания
     * @param rangeEnd   конец диапазона дат создания
     * @param from       начальный индекс для пагинации
     * @param size       количество записей на странице
     * @return список DTO комментариев, соответствующих критериям поиска
     */
    List<CommentDto> searchComments(String text, String status, LocalDateTime rangeStart,
                                    LocalDateTime rangeEnd, Integer from, Integer size);
}