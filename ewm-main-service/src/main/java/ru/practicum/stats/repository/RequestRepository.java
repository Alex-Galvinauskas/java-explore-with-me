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

    List<Request> findByRequesterId(Long requesterId);

    List<Request> findByEventId(Long eventId);

    boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);

    Optional<Request> findByIdAndRequesterId(Long id, Long requesterId);

    @Query("SELECT COUNT(r) FROM Request r WHERE r.event.id = :eventId AND r.status = :status")
    Long countByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") RequestStatus status);

    @Query("SELECT r.event.id, COUNT(r) FROM Request r " +
            "WHERE r.event.id IN :eventIds AND r.status = :status " +
            "GROUP BY r.event.id")
    List<Object[]> countByEventIdsAndStatus(@Param("eventIds") List<Long> eventIds,
                                            @Param("status") RequestStatus status);

    boolean existsByEventIdAndRequesterIdAndStatus(Long eventId, Long requesterId, RequestStatus status);
}