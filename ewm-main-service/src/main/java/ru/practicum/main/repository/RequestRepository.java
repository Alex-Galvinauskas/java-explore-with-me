package ru.practicum.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.main.model.Request;
import ru.practicum.main.model.enums.RequestStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findByRequesterId(Long userId);

    Optional<Request> findByIdAndRequesterId(Long requestId, Long userId);

    boolean existsByEventIdAndRequesterId(Long eventId, Long userId);

    long countByEventIdAndStatus(Long eventId, RequestStatus status);

    List<Request> findAllByIdIn(List<Long> ids);

    @Query("SELECT r FROM Request r WHERE r.event.id = :eventId")
    List<Request> findAllByEventId(@Param("eventId") Long eventId);


    @Query("SELECT r FROM Request r WHERE r.event.id = :eventId AND r.status = :status")
    List<Request> findAllByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") RequestStatus status);

    @Query("SELECT r.event.id, COUNT(r) FROM Request r " +
            "WHERE r.event.id IN :eventIds AND r.status = :status " +
            "GROUP BY r.event.id")
    List<Object[]> countByEventIdsAndStatus(@Param("eventIds") List<Long> eventIds,
                                            @Param("status") RequestStatus status);
}