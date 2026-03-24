package ru.practicum.stats.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.stats.model.Event;
import ru.practicum.stats.model.enums.EventState;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends BaseRepository<Event> {

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Event e WHERE e.category.id = :categoryId")
    boolean existsByCategoryId(@Param("categoryId") Long categoryId);

    @Query("SELECT e FROM Event e " +
            "JOIN FETCH e.category " +
            "JOIN FETCH e.initiator " +
            "WHERE e.initiator.id = :userId")
    List<Event> findByInitiatorId(@Param("userId") Long userId, Pageable pageable);

    @Query(value = "SELECT e.* FROM events e " +
            "LEFT JOIN categories c ON e.category_id = c.id " +
            "LEFT JOIN users u ON e.initiator_id = u.id " +
            "WHERE e.state = :state " +
            "AND (cast(:text as text) IS NULL OR " +
            "   (LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%')) OR " +
            "    LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%')))) " +
            "AND (cast(:categories as text) IS NULL OR " +
            "   e.category_id IN (:categories)) " +
            "AND (cast(:paid as boolean) IS NULL OR e.paid = :paid) " +
            "AND (e.event_date >= :rangeStart) " +
            "AND (e.event_date <= :rangeEnd) " +
            "AND (:onlyAvailable = false OR " +
            "   e.participant_limit = 0 OR " +
            "   e.confirmed_requests < e.participant_limit) " +
            "ORDER BY e.event_date ASC", nativeQuery = true)
    List<Event> findPublishedEvents(
            @Param("state") String state,
            @Param("text") String text,
            @Param("categories") List<Long> categories,
            @Param("paid") Boolean paid,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            @Param("onlyAvailable") Boolean onlyAvailable,
            Pageable pageable
    );

    @Query("SELECT e FROM Event e " +
            "JOIN FETCH e.category " +
            "JOIN FETCH e.initiator " +
            "WHERE (:users IS NULL OR e.initiator.id IN :users) " +
            "AND (:states IS NULL OR e.state IN :states) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (e.eventDate >= :rangeStart) " +
            "AND (e.eventDate <= :rangeEnd)")
    List<Event> findEventsByAdmin(
            @Param("users") List<Long> users,
            @Param("states") List<EventState> states,
            @Param("categories") List<Long> categories,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable
    );

    @Query("SELECT e FROM Event e " +
            "JOIN FETCH e.category " +
            "JOIN FETCH e.initiator " +
            "WHERE e.id = :eventId")
    Event findByIdWithDetails(@Param("eventId") Long eventId);
}