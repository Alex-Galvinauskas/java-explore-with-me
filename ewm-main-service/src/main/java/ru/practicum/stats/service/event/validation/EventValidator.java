package ru.practicum.stats.service.event.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.stats.exception.ConflictException;
import ru.practicum.stats.exception.NotFoundException;
import ru.practicum.stats.exception.ValidationException;
import ru.practicum.stats.model.Event;
import ru.practicum.stats.model.User;
import ru.practicum.stats.model.enums.EventState;
import ru.practicum.stats.repository.CategoryRepository;
import ru.practicum.stats.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventValidator {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public User validateAndGetUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }

    public void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }

    public void validateCategoryExists(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new NotFoundException("Категория с id=" + categoryId + " не найдена");
        }
    }

    public LocalDateTime parseDate(String dateString) {
        try {
            return LocalDateTime.parse(dateString, FORMATTER);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Неверный формат даты. Ожидается: yyyy-MM-dd HH:mm:ss");
        }
    }

    public LocalDateTime parseAndValidateEventDate(String dateString, int minHoursFromNow) {
        LocalDateTime eventDate = parseDate(dateString);
        validateEventDateNotInPast(eventDate, minHoursFromNow);
        return eventDate;
    }

    public void validateEventDateNotInPast(LocalDateTime eventDate, int minHoursFromNow) {
        LocalDateTime minAllowedDate = LocalDateTime.now().plusHours(minHoursFromNow);
        if (eventDate.isBefore(minAllowedDate)) {
            throw new ConflictException(
                    String.format("Дата события должна быть не раньше чем через %d часа(ов) от текущего момента",
                            minHoursFromNow)
            );
        }
    }

    public void validateEventNotPublished(Event event) {
        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Нельзя изменить опубликованное событие");
        }
    }

    public void validateUserIsInitiator(Event event, Long userId) {
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ValidationException("Пользователь не является инициатором события");
        }
    }

    public void validateAdminEventDateUpdate(Event event, LocalDateTime newDate) {
        if (newDate.isBefore(LocalDateTime.now().plusHours(1))) {
            throw new ConflictException("Дата события должна быть не раньше чем через 1 час от текущего момента");
        }

        if (event.getState() == EventState.PUBLISHED && newDate.isBefore(event.getEventDate())) {
            throw new ConflictException("Дата опубликованного события не может быть изменена на более раннюю");
        }
    }

    public void validateParticipantLimit(Integer limit) {
        if (limit != null && limit < 0) {
            throw new ValidationException("Лимит участников не может быть отрицательным");
        }
    }

    public void validatePagination(int from, int size) {
        if (from < 0) {
            throw new ValidationException("Параметр 'from' не может быть отрицательным");
        }
        if (size <= 0) {
            throw new ValidationException("Параметр 'size' должен быть положительным");
        }
    }
}