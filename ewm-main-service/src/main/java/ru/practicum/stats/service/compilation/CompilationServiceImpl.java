package ru.practicum.stats.service.compilation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.core.compilation.loader.CompilationEventLoader;
import ru.practicum.stats.core.compilation.mapper.CompilationMapperHelper;
import ru.practicum.stats.dto.compilation.CompilationDto;
import ru.practicum.stats.dto.compilation.NewCompilationDto;
import ru.practicum.stats.dto.compilation.UpdateCompilationRequest;
import ru.practicum.stats.mapper.CompilationMapper;
import ru.practicum.stats.model.Compilation;
import ru.practicum.stats.model.Event;
import ru.practicum.stats.repository.CompilationRepository;
import ru.practicum.stats.validation.compilation.CompilationValidator;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final CompilationValidator validationService;
    private final CompilationEventLoader eventLoader;
    private final CompilationMapperHelper mapperHelper;

    @Override
    @Transactional
    public CompilationDto saveCompilation(NewCompilationDto newCompilationDto) {
        log.info("Создание новой подборки: {}", newCompilationDto);

        NewCompilationDto preparedDto = validationService.prepareNewCompilationDto(newCompilationDto);
        Compilation compilation = compilationMapper.toEntity(preparedDto);

        Set<Event> events = eventLoader.loadEventsByIds(preparedDto.getEvents());
        compilation.setEvents(events);

        Compilation savedCompilation = compilationRepository.save(compilation);
        log.info("Подборка успешно создана с id: {}", savedCompilation.getId());

        return mapperHelper.toDtoWithEvents(savedCompilation, events);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        log.info("Удаление подборки с id: {}", compId);

        validationService.validateCompilationExists(compId);
        compilationRepository.deleteById(compId);

        log.info("Подборка с id {} успешно удалена", compId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest request) {
        log.info("Обновление подборки с id: {}, request: {}", compId, request);

        Compilation compilation = validationService.validateAndGetCompilation(compId);

        updateCompilationFields(compilation, request);

        Set<Event> events = updateCompilationEvents(compilation, request.getEvents());
        compilation.setEvents(events);

        Compilation updatedCompilation = compilationRepository.save(compilation);
        log.info("Подборка с id {} успешно обновлена", compId);

        return mapperHelper.toDtoWithEvents(updatedCompilation, events);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Pageable pageable) {
        log.info("Получение подборок с фильтром pinned: {}, pageable: {}", pinned, pageable);

        Page<Compilation> compilationPage = getCompilationsPage(pinned, pageable);
        List<Compilation> compilations = compilationPage.getContent();

        if (compilations.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Set<Event>> eventsByCompilation = eventLoader.loadEventsForCompilations(compilations);

        log.info("Найдено {} подборок", compilations.size());
        return mapperHelper.toDtoListWithEvents(compilations, eventsByCompilation);
    }

    @Override
    public CompilationDto getCompilation(Long compId) {
        log.info("Получение подборки по id: {}", compId);

        Compilation compilation = validationService.validateAndGetCompilation(compId);
        Set<Event> events = eventLoader.loadEventsByIds(
                compilation.getEvents().stream()
                        .map(Event::getId)
                        .toList()
        );

        return mapperHelper.toDtoWithEvents(compilation, events);
    }

    private Page<Compilation> getCompilationsPage(Boolean pinned, Pageable pageable) {
        if (pinned == null) {
            return compilationRepository.findAll(pageable);
        }
        return compilationRepository.findByPinned(pinned, pageable);
    }

    private void updateCompilationFields(Compilation compilation, UpdateCompilationRequest request) {
        if (request.getTitle() != null) {
            validationService.validateTitleLength(request.getTitle());
            compilation.setTitle(request.getTitle());
        }

        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }
    }

    private Set<Event> updateCompilationEvents(Compilation compilation, List<Long> eventIds) {
        if (eventIds == null) {
            return compilation.getEvents();
        }

        if (eventIds.isEmpty()) {
            return new HashSet<>();
        }

        return eventLoader.loadEventsByIds(eventIds);
    }
}