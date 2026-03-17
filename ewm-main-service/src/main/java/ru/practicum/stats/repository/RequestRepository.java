package ru.practicum.stats.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.stats.model.Request;
import ru.practicum.stats.model.enums.RequestStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends BaseRepository<Request> {

    List<Request> findByRequesterId(Long requesterId);

    boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);

    long countByEventIdAndStatus(Long eventId, RequestStatus status);

    Optional<Request> findByIdAndRequesterId(Long id, Long requesterId);
}