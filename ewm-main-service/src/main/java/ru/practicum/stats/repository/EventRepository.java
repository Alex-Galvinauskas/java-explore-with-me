package ru.practicum.stats.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.stats.model.Event;
import ru.practicum.stats.model.enums.EventState;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Event e WHERE e.category.id = :categoryId")
    boolean existsByCategoryId(@Param("categoryId") Long categoryId);

    List<Event> findByInitiatorId(Long userId, Pageable pageable);

    @Query("SELECT e FROM Event e " +
            "WHERE e.state = :state " +
            "AND (:text IS NULL OR " +
            "   (LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%')) OR " +
            "    LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%')))) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND (e.eventDate >= :rangeStart) " +
            "AND (e.eventDate <= :rangeEnd) " +
            "AND (:onlyAvailable = false OR " +
            "   e.participantLimit = 0 OR " +
            "   e.confirmedRequests < e.participantLimit) " +
            "ORDER BY e.eventDate ASC")
    List<Event> findPublishedEvents(
            @Param("state") EventState state,
            @Param("text") String text,
            @Param("categories") List<Long> categories,
            @Param("paid") Boolean paid,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            @Param("onlyAvailable") Boolean onlyAvailable,
            Pageable pageable
    );

    @Query("SELECT e FROM Event e " +
            "WHERE e.state = :state " +
            "AND (:text IS NULL OR " +
            "   (LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%')) OR " +
            "    LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%')))) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND (e.eventDate >= :rangeStart) " +
            "AND (e.eventDate <= :rangeEnd) " +
            "AND (:onlyAvailable = false OR " +
            "   e.participantLimit = 0 OR " +
            "   e.confirmedRequests < e.participantLimit)")
    List<Event> findPublishedEvents(
            @Param("state") EventState state,
            @Param("text") String text,
            @Param("categories") List<Long> categories,
            @Param("paid") Boolean paid,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            @Param("onlyAvailable") Boolean onlyAvailable
    );

    @Query("SELECT e FROM Event e " +
            "WHERE (:users IS NULL OR e.initiator.id IN :users) " +
            "AND (:states IS NULL OR e.state IN :states) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (CAST(:rangeStart AS date) IS NULL OR e.eventDate >= :rangeStart) " +
            "AND (CAST(:rangeEnd AS date) IS NULL OR e.eventDate <= :rangeEnd)")
    List<Event> findEventsByAdmin(
            @Param("users") List<Long> users,
            @Param("states") List<EventState> states,
            @Param("categories") List<Long> categories,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable
    );
}