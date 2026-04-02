package ru.practicum.stats.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.stats.model.Comment;
import ru.practicum.stats.model.enums.CommentStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByEventIdAndStatusOrderByCreatedOnAsc(Long eventId, CommentStatus status, Pageable pageable);

    List<Comment> findByAuthorId(Long authorId, Pageable pageable);

    List<Comment> findByEventIdAndAuthorId(Long eventId, Long authorId);

    @Query("SELECT c FROM Comment c WHERE LOWER(c.text) LIKE LOWER(CONCAT('%', :text, '%')) ORDER BY c.createdOn DESC")
    List<Comment> findByTextContainingIgnoreCase(@Param("text") String text, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE LOWER(c.text) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "AND c.status = :status ORDER BY c.createdOn DESC")
    List<Comment> findByTextContainingIgnoreCaseAndStatus(@Param("text") String text, @Param("status")
    CommentStatus status, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE LOWER(c.text) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "AND c.createdOn >= :rangeStart AND c.createdOn <= :rangeEnd ORDER BY c.createdOn DESC")
    List<Comment> findByTextContainingIgnoreCaseAndCreatedOnBetween(@Param("text") String text,
                                                                    @Param("rangeStart") LocalDateTime rangeStart,
                                                                    @Param("rangeEnd") LocalDateTime rangeEnd,
                                                                    Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE LOWER(c.text) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "AND c.status = :status AND c.createdOn >= :rangeStart AND c.createdOn <= :rangeEnd ORDER BY c.createdOn DESC")
    List<Comment> findByTextContainingIgnoreCaseAndStatusAndCreatedOnBetween(@Param("text")
                                                                             String text, @Param("status")
    CommentStatus status, @Param("rangeStart") LocalDateTime rangeStart, @Param("rangeEnd") LocalDateTime rangeEnd,
                                                                             Pageable pageable);

    List<Comment> findByStatus(CommentStatus status, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.status = :status " +
            "AND c.createdOn >= :rangeStart AND c.createdOn <= :rangeEnd ORDER BY c.createdOn DESC")
    List<Comment> findByStatusAndCreatedOnBetween(@Param("status") CommentStatus status, @Param("rangeStart")
    LocalDateTime rangeStart, @Param("rangeEnd") LocalDateTime rangeEnd, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.createdOn >= :rangeStart " +
            "AND c.createdOn <= :rangeEnd ORDER BY c.createdOn DESC")
    List<Comment> findByCreatedOnBetween(@Param("rangeStart") LocalDateTime rangeStart,
                                         @Param("rangeEnd") LocalDateTime rangeEnd, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE LOWER(c.text) LIKE LOWER(CONCAT('%', :text, '%'))" +
            " AND c.createdOn >= :rangeStart AND c.createdOn <= :rangeEnd ORDER BY c.createdOn DESC")
    List<Comment> findByTextContainingIgnoreCaseAndCreatedOnBetweenSimple(@Param("text")
                                                                          String text, @Param("rangeStart")
    LocalDateTime rangeStart, @Param("rangeEnd") LocalDateTime rangeEnd, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.event.id IN :eventIds AND c.status = 'PUBLISHED'")
    List<Comment> findByEventIdsAndPublished(@Param("eventIds") List<Long> eventIds);

}