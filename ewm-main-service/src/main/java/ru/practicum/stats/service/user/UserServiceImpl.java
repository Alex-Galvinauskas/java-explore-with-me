package ru.practicum.stats.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.dto.user.NewUserRequest;
import ru.practicum.stats.dto.user.UserDto;
import ru.practicum.stats.exception.ConflictException;
import ru.practicum.stats.exception.NotFoundException;
import ru.practicum.stats.mapper.UserMapper;
import ru.practicum.stats.model.User;
import ru.practicum.stats.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto registerUser(NewUserRequest newUserRequest) {
        log.info("Регистрация нового пользователя: {}, email: {}", newUserRequest.getName(), newUserRequest.getEmail());

        User user = userMapper.toEntity(newUserRequest);

        try {
            User savedUser = userRepository.save(user);
            log.info("Пользователь успешно зарегистрирован с id: {}", savedUser.getId());
            return userMapper.toDto(savedUser);
        } catch (DataIntegrityViolationException e) {
            String message = String.format("Пользователь с email '%s' уже существует", newUserRequest.getEmail());

            if (e.getMessage() != null && e.getMessage().contains("uq_email")) {
                log.warn("Нарушение уникальности email: {}", newUserRequest.getEmail());
                throw new ConflictException(message);
            }
            log.error("Ошибка целостности данных при регистрации пользователя", e);
            throw new ConflictException("Ошибка при регистрации пользователя: " + e.getMessage());
        }
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        log.info("Получение списка пользователей: ids={}, from={}, size={}", ids, from, size);

        Pageable pageable = PageRequest.of(from / size, size);

        List<User> users;
        if (ids != null && !ids.isEmpty()) {
            users = userRepository.findAllById(ids);
            users = users.stream()
                    .skip(from)
                    .limit(size)
                    .collect(Collectors.toList());
        } else {
            users = userRepository.findAll(pageable).getContent();
        }

        log.info("Найдено {} пользователей", users.size());
        return users.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Удаление пользователя с id: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        try {
            userRepository.deleteById(userId);
            log.info("Пользователь с id: {} успешно удален", userId);
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка при удалении пользователя с id: {}", userId, e);
            throw new ConflictException("Невозможно удалить пользователя с id=" + userId);
        }
    }
}