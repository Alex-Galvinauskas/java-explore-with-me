package ru.practicum.stats.service.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.dto.comment.CommentDto;
import ru.practicum.stats.dto.comment.NewCommentDto;
import ru.practicum.stats.dto.comment.UpdateCommentDto;
import ru.practicum.stats.mapper.CommentMapper;
import ru.practicum.stats.model.Comment;
import ru.practicum.stats.model.Event;
import ru.practicum.stats.model.enums.CommentStatus;
import ru.practicum.stats.repository.CommentRepository;
import ru.practicum.stats.validation.comment.CommentValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final CommentValidator commentValidator;

    @Override
    @Transactional
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto dto) {
        commentValidator.validateUserExists(userId);
        Event event = commentValidator.validateEventForCommenting(eventId);

        Comment comment = commentMapper.toNewEntity(dto, eventId, userId);
        Comment saved = commentRepository.save(comment);
        log.info("Created comment with id={} for event id={} by user id={}", saved.getId(), eventId, userId);

        return commentMapper.toDto(saved);
    }

    @Override
    public CommentDto getCommentById(Long commentId) {
        Comment comment = commentValidator.validateCommentExists(commentId);
        commentValidator.validateCommentIsPublished(comment);

        return commentMapper.toDto(comment);
    }

    @Override
    @Transactional
    public CommentDto updateCommentByAuthor(Long userId, Long commentId, UpdateCommentDto dto) {
        Comment comment = commentValidator.validateCommentExists(commentId);
        commentValidator.validateAuthorAccess(userId, comment);
        commentValidator.validateCommentEditable(comment);

        commentMapper.updateEntityFromDto(dto, comment);
        Comment updated = commentRepository.save(comment);
        log.info("Updated comment id={} by user id={}", commentId, userId);

        return commentMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void deleteCommentByAuthor(Long userId, Long commentId) {
        Comment comment = commentValidator.validateCommentExists(commentId);
        commentValidator.validateAuthorAccess(userId, comment);
        commentValidator.validateCommentDeletableByAuthor(comment);

        comment.setStatus(CommentStatus.DELETED);
        comment.setUpdatedOn(LocalDateTime.now());
        commentRepository.save(comment);
        log.info("Soft deleted comment id={} by user id={}", commentId, userId);
    }

    @Override
    @Transactional
    public void deleteCommentByAdmin(Long commentId) {
        Comment comment = commentValidator.validateCommentExists(commentId);
        commentRepository.delete(comment);
        log.info("Hard deleted comment id={} by admin", commentId);
    }

    @Override
    @Transactional
    public CommentDto publishComment(Long commentId) {
        Comment comment = commentValidator.validateCommentExists(commentId);
        commentValidator.validateCommentPublishable(comment);

        comment.setStatus(CommentStatus.PUBLISHED);
        comment.setUpdatedOn(LocalDateTime.now());
        Comment saved = commentRepository.save(comment);
        log.info("Published comment id={}", commentId);

        return commentMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CommentDto rejectComment(Long commentId) {
        Comment comment = commentValidator.validateCommentExists(commentId);
        commentValidator.validateCommentRejectable(comment);

        comment.setStatus(CommentStatus.REJECTED);
        comment.setUpdatedOn(LocalDateTime.now());
        Comment saved = commentRepository.save(comment);
        log.info("Rejected comment id={}", commentId);

        return commentMapper.toDto(saved);
    }

    @Override
    public List<CommentDto> getCommentsByEventId(Long eventId, Integer from, Integer size) {
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);

        List<Comment> comments = commentRepository.findByEventIdAndStatusOrderByCreatedOnAsc(
                eventId, CommentStatus.PUBLISHED, pageable);

        return comments.stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentDto> getCommentsByUserId(Long userId, Integer from, Integer size) {
        commentValidator.validateUserExists(userId);

        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);
        List<Comment> comments = commentRepository.findByAuthorId(userId, pageable);

        return comments.stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentDto> searchComments(String text, String status, LocalDateTime rangeStart,
                                           LocalDateTime rangeEnd, Integer from, Integer size) {
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);

        CommentStatus commentStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                commentStatus = CommentStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status value: {}, ignoring filter", status);
            }
        }

        List<Comment> comments = getCommentsByFilters(text, commentStatus, rangeStart, rangeEnd, pageable);

        return comments.stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    private List<Comment> getCommentsByFilters(String text, CommentStatus status,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               Pageable pageable) {
        boolean hasText = text != null && !text.isEmpty();
        boolean hasStatus = status != null;
        boolean hasDates = rangeStart != null && rangeEnd != null;

        if (hasText && hasStatus && hasDates) {
            return commentRepository.findByTextContainingIgnoreCaseAndStatusAndCreatedOnBetween(
                    text, status, rangeStart, rangeEnd, pageable);
        } else if (hasText && hasStatus) {
            return commentRepository.findByTextContainingIgnoreCaseAndStatus(text, status, pageable);
        } else if (hasText && hasDates) {
            return commentRepository
                    .findByTextContainingIgnoreCaseAndCreatedOnBetween(text, rangeStart, rangeEnd, pageable);
        } else if (hasStatus && hasDates) {
            return commentRepository.findByStatusAndCreatedOnBetween(status, rangeStart, rangeEnd, pageable);
        } else if (hasText) {
            return commentRepository.findByTextContainingIgnoreCase(text, pageable);
        } else if (hasStatus) {
            return commentRepository.findByStatus(status, pageable);
        } else if (hasDates) {
            return commentRepository.findByCreatedOnBetween(rangeStart, rangeEnd, pageable);
        } else {
            return commentRepository.findAll(pageable).getContent();
        }
    }
}