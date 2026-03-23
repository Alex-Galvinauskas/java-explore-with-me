package ru.practicum.stats.core.compilation.loader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.stats.exception.NotFoundException;
import ru.practicum.stats.model.Compilation;
import ru.practicum.stats.model.Event;
import ru.practicum.stats.repository.EventRepository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompilationEventLoader {

    private final EventRepository eventRepository;

    public Set<Event> loadEventsByIds(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return new HashSet<>();
        }

        List<Event> events = eventRepository.findAllById(eventIds);

        if (events.size() != eventIds.size()) {
            Set<Long> foundIds = events.stream()
                    .map(Event::getId)
                    .collect(Collectors.toSet());
            List<Long> notFoundIds = eventIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();
            throw new NotFoundException("События с id " + notFoundIds + " не найдены");
        }

        return new HashSet<>(events);
    }

    public Map<Long, Set<Event>> loadEventsForCompilations(List<Compilation> compilations) {
        List<Long> allEventIds = compilations.stream()
                .flatMap(compilation -> compilation.getEvents().stream())
                .map(Event::getId)
                .distinct()
                .collect(Collectors.toList());

        if (allEventIds.isEmpty()) {
            return new HashMap<>();
        }

        List<Event> allEvents = eventRepository.findAllById(allEventIds);
        Map<Long, Set<Event>> eventsByCompilation = new HashMap<>();

        for (Compilation compilation : compilations) {
            Set<Event> compilationEvents = allEvents.stream()
                    .filter(event -> compilation.getEvents().stream()
                            .anyMatch(compEvent -> compEvent.getId().equals(event.getId())))
                    .collect(Collectors.toSet());
            eventsByCompilation.put(compilation.getId(), compilationEvents);
        }

        return eventsByCompilation;
    }
}