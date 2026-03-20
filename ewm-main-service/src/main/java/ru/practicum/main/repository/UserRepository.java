package ru.practicum.main.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.main.model.User;

import java.util.Optional;

@Repository
public interface UserRepository extends BaseRepository<User> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}