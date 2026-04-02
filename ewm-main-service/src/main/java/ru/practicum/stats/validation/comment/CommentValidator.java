package ru.practicum.stats.validation.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.stats.exception.CommentNotFoundException;
import ru.practicum.stats.exception.ConflictException;
import ru.practicum.stats.exception.ForbiddenException;
import ru.practicum.stats.exception.NotFoundException;
import ru.practicum.stats.model.Comment;
import ru.practicum.stats.model.Event;
import ru.practicum.stats.model.User;
import ru.practicum.stats.model.enums.CommentStatus;
import ru.practicum.stats.model.enums.EventState;
import ru.practicum.stats.repository.CommentRepository;
import ru.practicum.stats.repository.EventRepository;
import ru.practicum.stats.repository.UserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentValidator {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    /**
     * Проверяет, что событие существует и опубликовано для комментирования.
     *
     * @param eventId идентификатор события
     * @return найденное событие
     * @throws NotFoundException если событие не найдено
     * @throws ConflictException если событие не опубликовано
     */
    public Event validateEventForCommenting(Long eventId) {
        Event event = getEventOrThrow(eventId);

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Cannot comment on unpublished event");
        }

        return event;
    }

    /**
     * Проверяет, что пользователь существует.
     *
     * @param userId идентификатор пользователя
     * @return найденный пользователь
     * @throws NotFoundException если пользователь не найден
     */
    public User validateUserExists(Long userId) {
        return getUserOrThrow(userId);
    }

    /**
     * Проверяет, что комментарий существует.
     *
     * @param commentId идентификатор комментария
     * @return найденный комментарий
     * @throws CommentNotFoundException если комментарий не найден
     */
    public Comment validateCommentExists(Long commentId) {
        return getCommentOrThrow(commentId);
    }

    /**
     * Проверяет, что комментарий опубликован (доступен для просмотра).
     *
     * @param comment комментарий для проверки
     * @throws NotFoundException если комментарий не опубликован
     */
    public void validateCommentIsPublished(Comment comment) {
        if (!comment.getStatus().equals(CommentStatus.PUBLISHED)) {
            throw new NotFoundException("Comment is not published or not found");
        }
    }

    /**
     * Проверяет, что пользователь является автором комментария.
     *
     * @param userId   идентификатор пользователя
     * @param comment комментарий
     * @throws ForbiddenException если пользователь не автор
     */
    public void validateAuthorAccess(Long userId, Comment comment) {
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("Only author can perform this action on comment");
        }
    }

    /**
     * Проверяет, что комментарий можно редактировать.
     * Редактировать можно только комментарии в статусе PENDING или REJECTED.
     *
     * @param comment комментарий для проверки
     * @throws ConflictException если комментарий нельзя редактировать
     */
    public void validateCommentEditable(Comment comment) {
        switch (comment.getStatus()) {
            case DELETED:
                throw new ConflictException("Cannot edit deleted comment");
            case PUBLISHED:
                throw new ConflictException("Cannot edit published comment");
            default:
        }
    }

    /**
     * Проверяет, что комментарий можно удалить автором.
     * Автор может удалить только комментарии в статусе PENDING или REJECTED.
     *
     * @param comment комментарий для проверки
     * @throws ConflictException если комментарий нельзя удалить
     */
    public void validateCommentDeletableByAuthor(Comment comment) {
        if (comment.getStatus().equals(CommentStatus.PUBLISHED)) {
            throw new ConflictException("Cannot delete published comment");
        }
    }

    /**
     * Проверяет, что комментарий можно опубликовать.
     * Опубликовать можно только комментарии в статусе PENDING.
     *
     * @param comment комментарий для проверки
     * @throws ConflictException если комментарий нельзя опубликовать
     */
    public void validateCommentPublishable(Comment comment) {
        if (comment.getStatus() != CommentStatus.PENDING) {
            throw new ConflictException("Only PENDING comments can be published");
        }
    }

    /**
     * Проверяет, что комментарий можно отклонить.
     * Отклонить можно только комментарии в статусе PENDING.
     *
     * @param comment комментарий для проверки
     * @throws ConflictException если комментарий нельзя отклонить
     */
    public void validateCommentRejectable(Comment comment) {
        if (comment.getStatus() != CommentStatus.PENDING) {
            throw new ConflictException("Only PENDING comments can be rejected");
        }
    }

    private Comment getCommentOrThrow(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(id));
    }

    private Event getEventOrThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event not found with id: " + id));
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }
}