package ru.practicum.stats.service.event.search;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.practicum.stats.exception.ValidationException;
import ru.practicum.stats.service.event.EventSearchParams;
import ru.practicum.stats.service.event.validation.PaginationValidator;

import java.time.LocalDateTime;
import java.util.List;

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

        if (params.getFrom() != null || params.getSize() != null) {
            paginationValidator.validate(
                    params.getFrom() != null ? params.getFrom() : 0,
                    params.getSize() != null ? params.getSize() : 10
            );
        }
    }

    public EventSearchParams preparePublicSearchParams(EventSearchParams params) {
        LocalDateTime rangeStart = params.getRangeStart();
        LocalDateTime rangeEnd = params.getRangeEnd();

        if (rangeStart == null && rangeEnd == null) {
            rangeStart = LocalDateTime.now();
            rangeEnd = LocalDateTime.now().plusYears(100);
        } else if (rangeStart == null) {
            rangeStart = LocalDateTime.now().minusYears(100);
        } else if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(100);
        }

        return EventSearchParams.builder()
                .text(params.getText())
                .categories(params.getCategories())
                .paid(params.getPaid())
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(params.getOnlyAvailable() != null ? params.getOnlyAvailable() : false)
                .sort(params.getSort())
                .from(params.getFrom())
                .size(params.getSize())
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

        return EventSearchParams.builder()
                .users(params.getUsers())
                .states(params.getStates())
                .categories(params.getCategories())
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .from(params.getFrom())
                .size(params.getSize())
                .build();
    }

    public Pageable createPageable(EventSearchParams params) {
        return paginationValidator.createPageable(params.getFrom(), params.getSize());
    }

    public boolean shouldSortByViews(EventSearchParams params) {
        return "VIEWS".equalsIgnoreCase(params.getSort());
    }

}