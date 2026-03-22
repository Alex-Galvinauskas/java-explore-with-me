package ru.practicum.stats.service.event.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.practicum.stats.exception.ValidationException;
import ru.practicum.stats.service.event.EventSearchParams;
import ru.practicum.stats.service.event.validation.PaginationValidator;

import java.time.LocalDateTime;
import java.util.List;
@Slf4j
@Component
@RequiredArgsConstructor
public class EventSearchParamsProcessor {

    private final PaginationValidator paginationValidator;

    public void validateSearchParams(EventSearchParams params) {
        if (params.getRangeStart() != null && params.getRangeEnd() != null) {
            if (params.getRangeStart().isAfter(params.getRangeEnd())) {
                throw new ValidationException("Дата начала не может быть позже даты окончания");
            }
        }

        if (params.getSort() != null &&
                !List.of("EVENT_DATE", "VIEWS").contains(params.getSort())) {
            throw new ValidationException("Недопустимое значение сортировки: " + params.getSort());
        }

        int from = params.getFrom() != null ? params.getFrom() : 0;
        int size = params.getSize() != null ? params.getSize() : 10;
        paginationValidator.validate(from, size);
    }

    public EventSearchParams preparePublicSearchParams(EventSearchParams params) {
        LocalDateTime rangeStart = params.getRangeStart();
        LocalDateTime rangeEnd = params.getRangeEnd();

        if (rangeStart == null && rangeEnd == null) {
            rangeStart = LocalDateTime.now();
            rangeEnd = LocalDateTime.now().plusYears(100);
        } else if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        } else if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(100);
        }

        // ✅ Добавить эту проверку!
        if (rangeStart.isAfter(rangeEnd)) {
            log.warn("rangeStart ({}) is after rangeEnd ({}), swapping them", rangeStart, rangeEnd);
            LocalDateTime temp = rangeStart;
            rangeStart = rangeEnd;
            rangeEnd = temp;
        }

        Integer from = params.getFrom() != null ? params.getFrom() : 0;
        Integer size = params.getSize() != null ? params.getSize() : 10;

        return EventSearchParams.builder()
                .text(params.getText())
                .categories(params.getCategories())
                .paid(params.getPaid())
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(params.getOnlyAvailable() != null ? params.getOnlyAvailable() : false)
                .sort(params.getSort())
                .from(from)
                .size(size)
                .build();
    }


    public EventSearchParams prepareAdminSearchParams(EventSearchParams params) {
        LocalDateTime rangeStart = params.getRangeStart();
        LocalDateTime rangeEnd = params.getRangeEnd();

        if (rangeStart == null) {
            rangeStart = LocalDateTime.now().minusYears(100);
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(100);
        }

        // ✅ ДОБАВИТЬ ЭТУ ПРОВЕРКУ!
        if (rangeStart.isAfter(rangeEnd)) {
            log.warn("Админский поиск: rangeStart ({}) позже rangeEnd ({}), меняем их местами",
                    rangeStart, rangeEnd);
            LocalDateTime temp = rangeStart;
            rangeStart = rangeEnd;
            rangeEnd = temp;
        }

        Integer from = params.getFrom() != null ? params.getFrom() : 0;
        Integer size = params.getSize() != null ? params.getSize() : 10;

        return EventSearchParams.builder()
                .users(params.getUsers())
                .states(params.getStates())
                .categories(params.getCategories())
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .from(from)
                .size(size)
                .build();
    }

    public Pageable createPageable(EventSearchParams params) {
        int from = params.getFrom() != null ? params.getFrom() : 0;
        int size = params.getSize() != null ? params.getSize() : 10;
        return paginationValidator.createPageable(from, size);
    }

    public boolean shouldSortByViews(EventSearchParams params) {
        return "VIEWS".equalsIgnoreCase(params.getSort());
    }

}