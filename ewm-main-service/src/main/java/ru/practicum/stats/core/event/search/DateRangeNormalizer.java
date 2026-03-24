package ru.practicum.stats.core.event.search;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class DateRangeNormalizer {

    private static final int YEARS_AHEAD = 100;
    private static final int YEARS_BACK = 100;

    public NormalizedDateRange normalizeForPublic(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        LocalDateTime start = rangeStart;
        LocalDateTime end = rangeEnd;

        if (start == null && end == null) {
            start = LocalDateTime.now();
            end = LocalDateTime.now().plusYears(YEARS_AHEAD);
        } else if (start == null) {
            start = LocalDateTime.now();
        } else if (end == null) {
            end = LocalDateTime.now().plusYears(YEARS_AHEAD);
        }

        return swapIfNeeded(start, end);
    }

    public NormalizedDateRange normalizeForAdmin(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        LocalDateTime start = rangeStart;
        LocalDateTime end = rangeEnd;

        if (start == null) {
            start = LocalDateTime.now().minusYears(YEARS_BACK);
        }
        if (end == null) {
            end = LocalDateTime.now().plusYears(YEARS_AHEAD);
        }

        return swapIfNeeded(start, end);
    }

    private NormalizedDateRange swapIfNeeded(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            log.warn("rangeStart ({}) позже rangeEnd ({}), меняем их местами", start, end);
            return new NormalizedDateRange(end, start);
        }
        return new NormalizedDateRange(start, end);
    }

    public record NormalizedDateRange(LocalDateTime start, LocalDateTime end) {}
}