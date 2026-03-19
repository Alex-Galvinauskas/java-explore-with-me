package ru.practicum.stats.service.compilation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.dto.compilation.CompilationDto;
import ru.practicum.stats.dto.compilation.NewCompilationDto;
import ru.practicum.stats.dto.compilation.UpdateCompilationRequest;
import ru.practicum.stats.exception.NotFoundException;
import ru.practicum.stats.mapper.CompilationMapper;
import ru.practicum.stats.mapper.EventMapper;
import ru.practicum.stats.model.Compilation;
import ru.practicum.stats.model.Event;
import ru.practicum.stats.repository.CompilationRepository;
import ru.practicum.stats.repository.EventRepository;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;
    private final EventMapper eventMapper;

    @Override
    @Transactional
    public CompilationDto saveCompilation(NewCompilationDto newCompilationDto) {
        log.info("Создание новой подборки: {}", newCompilationDto);

        if (newCompilationDto.getPinned() == null) {
            newCompilationDto.setPinned(false);
        }

        Compilation compilation = compilationMapper.toEntity(newCompilationDto);

        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            Set<Event> events = findEventsByIds(newCompilationDto.getEvents());
            compilation.setEvents(events);
        } else {
            compilation.setEvents(new HashSet<>());
        }

        Compilation savedCompilation = compilationRepository.save(compilation);
        log.info("Подборка успешно создана с id: {}", savedCompilation.getId());

        return compilationMapper.toDto(savedCompilation);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        log.info("Удаление подборки с id: {}", compId);

        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Подборка с id " + compId + " не найдена");
        }

        compilationRepository.deleteById(compId);
        log.info("Подборка с id {} успешно удалена", compId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest request) {
        log.info("Обновление подборки с id: {}, request: {}", compId, request);

        Compilation compilation = findCompilationById(compId);

        if (request.getTitle() != null) {
            compilation.setTitle(request.getTitle());
        }

        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }

        if (request.getEvents() != null) {
            if (request.getEvents().isEmpty()) {
                compilation.setEvents(new HashSet<>());
            } else {
                Set<Event> events = findEventsByIds(request.getEvents());
                compilation.setEvents(events);
            }
        }

        Compilation updatedCompilation = compilationRepository.save(compilation);
        log.info("Подборка с id {} успешно обновлена", compId);

        return compilationMapper.toDto(updatedCompilation);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Pageable pageable) {
        log.info("Получение подборок с фильтром pinned: {}, pageable: {}", pinned, pageable);

        List<Compilation> compilations;
        if (pinned == null) {
            compilations = compilationRepository.findAll(pageable).getContent();
        } else {
            compilations = compilationRepository.findAll(pageable).getContent()
                    .stream()
                    .filter(c -> c.getPinned().equals(pinned))
                    .collect(Collectors.toList());
        }

        if (compilations.isEmpty()) {
            return Collections.emptyList();
        }

        log.info("Найдено {} подборок", compilations.size());
        return compilations.stream()
                .map(compilationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilation(Long compId) {
        log.info("Получение подборки по id: {}", compId);

        Compilation compilation = findCompilationById(compId);
        return compilationMapper.toDto(compilation);
    }

    private Compilation findCompilationById(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id " + compId + " не найдена"));
    }

    private Set<Event> findEventsByIds(List<Long> eventIds) {
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
}