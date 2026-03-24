package ru.practicum.stats.service.user;

import ru.practicum.stats.dto.user.NewUserRequest;
import ru.practicum.stats.dto.user.UserDto;

import java.util.List;

/**
 * Сервис для управления пользователями.
 * Предоставляет операции регистрации, получения и удаления пользователей.
 */
public interface UserService {

    /**
     * Регистрирует нового пользователя.
     *
     * @param newUserRequest данные нового пользователя
     * @return зарегистрированный пользователь
     * @throws ru.practicum.stats.exception.ConflictException если пользователь с таким email уже существует
     */
    UserDto registerUser(NewUserRequest newUserRequest);

    /**
     * Возвращает список пользователей с фильтрацией по идентификаторам и пагинацией.
     *
     * @param ids  список идентификаторов пользователей (если null - все пользователи)
     * @param from начальная позиция
     * @param size количество элементов
     * @return список пользователей
     */
    List<UserDto> getUsers(List<Long> ids, int from, int size);

    /**
     * Удаляет пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @throws ru.practicum.stats.exception.NotFoundException если пользователь не найден
     */
    void deleteUser(Long userId);
}