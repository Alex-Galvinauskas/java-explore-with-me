package ru.practicum.stats.core.user.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.stats.model.User;
import ru.practicum.stats.repository.UserRepository;

import java.util.List;

/**
 * Отвечает только за поиск и фильтрацию.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserQueryService {
    private final UserRepository userRepository;

    /**
     * Находит пользователей по списку ID или всех с пагинацией.
     */
    public List<User> findUsers(List<Long> ids, int from, int size) {
        Pageable pageable = createPageable(from, size);

        if (hasIds(ids)) {
            return findUsersByIds(ids, pageable);
        } else {
            return findAllUsers(pageable);
        }
    }

    /**
     * Находит пользователей по ID с пагинацией.
     */
    private List<User> findUsersByIds(List<Long> ids, Pageable pageable) {
        Page<User> page = userRepository.findByIds(ids, pageable);
        log.debug("Найдено {} пользователей из запрошенных ID: {}", page.getNumberOfElements(), ids);
        return page.getContent();
    }

    /**
     * Находит всех пользователей с пагинацией.
     */
    private List<User> findAllUsers(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);
        log.debug("Найдено {} пользователей из общего списка", page.getNumberOfElements());
        return page.getContent();
    }

    /**
     * Создает объект пагинации.
     */
    private Pageable createPageable(int from, int size) {
        return PageRequest.of(from / size, size);
    }

    /**
     * Проверяет, есть ли ID для фильтрации.
     */
    private boolean hasIds(List<Long> ids) {
        return ids != null && !ids.isEmpty();
    }
}