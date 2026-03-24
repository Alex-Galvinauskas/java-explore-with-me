package ru.practicum.stats.core.compilation.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.stats.dto.compilation.CompilationDto;
import ru.practicum.stats.mapper.CompilationMapper;
import ru.practicum.stats.mapper.EventMapper;
import ru.practicum.stats.model.Compilation;
import ru.practicum.stats.model.Event;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompilationMapperHelper {

    private final CompilationMapper compilationMapper;
    private final EventMapper eventMapper;

    public CompilationDto toDtoWithEvents(Compilation compilation, Set<Event> events) {
        CompilationDto dto = compilationMapper.toDto(compilation);
        dto.setEvents(events.stream()
                .map(eventMapper::toShortDto)
                .collect(Collectors.toList()));
        return dto;
    }

    public List<CompilationDto> toDtoListWithEvents(
            List<Compilation> compilations,
            Map<Long, Set<Event>> eventsByCompilation) {

        return compilations.stream()
                .map(compilation -> {
                    Set<Event> events =
                            eventsByCompilation.getOrDefault(compilation.getId(), Set.of());
                    return toDtoWithEvents(compilation, events);
                })
                .collect(Collectors.toList());
    }
}