package ru.practicum.stats.validation.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.practicum.stats.exception.ValidationException;

@Slf4j
@Component
public class PaginationValidator {

    private static final int DEFAULT_FROM = 0;
    private static final int DEFAULT_SIZE = 10;

    public void validate(int from, int size) {
        if (from < 0) {
            throw new ValidationException("Параметр 'from' не может быть отрицательным. Получено: " + from);
        }
        if (size <= 0) {
            throw new ValidationException("Параметр 'size' должен быть положительным. Получено: " + size);
        }
    }

    public Pageable createPageable(Integer from, Integer size) {
        int validFrom = from != null ? from : DEFAULT_FROM;
        int validSize = size != null ? size : DEFAULT_SIZE;

        validate(validFrom, validSize);

        int pageNumber = validFrom / validSize;
        return PageRequest.of(pageNumber, validSize);
    }

}