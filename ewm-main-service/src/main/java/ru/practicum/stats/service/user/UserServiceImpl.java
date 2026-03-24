package ru.practicum.stats.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.core.user.query.UserQueryService;
import ru.practicum.stats.dto.user.NewUserRequest;
import ru.practicum.stats.dto.user.UserDto;
import ru.practicum.stats.exception.ConflictException;
import ru.practicum.stats.mapper.UserMapper;
import ru.practicum.stats.model.User;
import ru.practicum.stats.repository.UserRepository;
import ru.practicum.stats.validation.user.UserValidatior;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserQueryService userQueryService;
    private final UserValidatior validationService;

    @Override
    @Transactional
    public UserDto registerUser(NewUserRequest newUserRequest) {
        log.info("Регистрация нового пользователя: {}", newUserRequest.getEmail());

        validationService.validateNewUser(newUserRequest);

        User user = userMapper.toEntity(newUserRequest);
        User savedUser = userRepository.save(user);

        log.info("Пользователь успешно зарегистрирован с id: {}", savedUser.getId());
        return userMapper.toDto(savedUser);
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        log.info("Получение списка пользователей: ids={}, from={}, size={}", ids, from, size);

        List<User> users = userQueryService.findUsers(ids, from, size);

        log.info("Найдено {} пользователей", users.size());
        return users.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Удаление пользователя с id: {}", userId);

        User user = userRepository.findByIdOrThrow(userId);

        try {
            userRepository.delete(user);
            log.info("Пользователь с id: {} успешно удален", userId);
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка при удалении пользователя с id: {}", userId, e);
            throw new ConflictException("Невозможно удалить пользователя с id=" + userId +
                    ". Возможно, у него есть связанные данные");
        }
    }
}