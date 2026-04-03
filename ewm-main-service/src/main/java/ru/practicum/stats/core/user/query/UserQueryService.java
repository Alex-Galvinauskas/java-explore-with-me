package ru.practicum.stats.core.user.query;

import ru.practicum.stats.model.User;

import java.util.List;

/**
 * Интерфейс для сервиса поиска и фильтрации пользователей.
 */
public interface UserQueryService {

    /**
     * Находит пользователей по списку ID или всех с пагинацией.
     */
    List<User> findUsers(List<Long> ids, int from, int size);
}