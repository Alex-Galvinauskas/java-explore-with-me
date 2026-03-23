package ru.practicum.stats.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.stats.model.Request;
import ru.practicum.stats.model.enums.RequestStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends BaseRepository<Request> {

    @Query("SELECT r FROM Request r " +
            "JOIN FETCH r.event " +
            "JOIN FETCH r.requester " +
            "WHERE r.requester.id = :userId")
    List<Request> findByRequesterId(@Param("userId") Long userId);

    @Query("SELECT r FROM Request r " +
            "JOIN FETCH r.event " +
            "JOIN FETCH r.requester " +
            "WHERE r.id = :requestId AND r.requester.id = :userId")
    Optional<Request> findByIdAndRequesterId(@Param("requestId") Long requestId,
                                             @Param("userId") Long userId);

    boolean existsByEventIdAndRequesterId(Long eventId, Long userId);

    long countByEventIdAndStatus(Long eventId, RequestStatus status);

    @Query("SELECT r FROM Request r " +
            "JOIN FETCH r.event " +
            "JOIN FETCH r.requester " +
            "WHERE r.id IN :ids")
    List<Request> findAllByIdIn(@Param("ids") List<Long> ids);

    @Query("SELECT r FROM Request r " +
            "JOIN FETCH r.event " +
            "JOIN FETCH r.requester " +
            "WHERE r.event.id = :eventId")
    List<Request> findAllByEventId(@Param("eventId") Long eventId);

    @Query("SELECT r FROM Request r " +
            "JOIN FETCH r.event " +
            "JOIN FETCH r.requester " +
            "WHERE r.event.id = :eventId AND r.status = :status")
    List<Request> findAllByEventIdAndStatus(@Param("eventId") Long eventId,
                                            @Param("status") RequestStatus status);

    @Query("SELECT r.event.id, COUNT(r) FROM Request r " +
            "WHERE r.event.id IN :eventIds AND r.status = :status " +
            "GROUP BY r.event.id")
    List<Object[]> countByEventIdsAndStatus(@Param("eventIds") List<Long> eventIds,
                                            @Param("status") RequestStatus status);
}