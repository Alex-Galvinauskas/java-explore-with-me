package ru.practicum.stats.core.event.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.practicum.stats.dto.event.EventSearchParams;
import ru.practicum.stats.exception.BadRequestException;
import ru.practicum.stats.exception.ValidationException;
import ru.practicum.stats.validation.event.PaginationValidator;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSearchParamsProcessor {

    private final PaginationValidator paginationValidator;
    private final DateRangeNormalizer dateRangeNormalizer;

    public void validateDateRange(EventSearchParams params) {
        LocalDateTime rangeStart = params.getRangeStart();
        LocalDateTime rangeEnd = params.getRangeEnd();

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new BadRequestException(
                    String.format("Дата начала (%s) не может быть позже даты окончания (%s)",
                            rangeStart, rangeEnd)
            );
        }
    }

    public void validateSearchParams(EventSearchParams params) {
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
        DateRangeNormalizer.NormalizedDateRange normalized = dateRangeNormalizer.normalizeForAdmin(
                params.getRangeStart(),
                params.getRangeEnd()
        );

        Integer from = params.getFrom() != null ? params.getFrom() : 0;
        Integer size = params.getSize() != null ? params.getSize() : 10;

        return EventSearchParams.builder()
                .users(params.getUsers())
                .states(params.getStates())
                .categories(params.getCategories())
                .rangeStart(normalized.start())
                .rangeEnd(normalized.end())
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