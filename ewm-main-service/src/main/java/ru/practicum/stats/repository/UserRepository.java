package ru.practicum.stats.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.stats.model.User;

@Repository
public interface UserRepository extends BaseRepository<User> {
}