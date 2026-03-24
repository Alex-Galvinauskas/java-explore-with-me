package ru.practicum.stats.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.stats.exception.ConflictException;
import ru.practicum.stats.exception.NotFoundException;
import ru.practicum.stats.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends BaseRepository<User> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.id IN :ids")
    Page<User> findByIds(@Param("ids") List<Long> ids, Pageable pageable);

    default User findByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    default void checkExistsByEmail(String email) {
        if (existsByEmail(email)) {
            throw new ConflictException(
                    String.format("Пользователь с email \"%s\" уже существует", email)
            );
        }
    }
}