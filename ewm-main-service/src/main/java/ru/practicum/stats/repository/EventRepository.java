package ru.practicum.stats.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.stats.model.Event;

@Repository
public interface EventRepository extends BaseRepository<Event> {
}