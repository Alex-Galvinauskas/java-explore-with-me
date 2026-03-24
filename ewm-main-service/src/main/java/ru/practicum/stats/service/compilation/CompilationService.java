package ru.practicum.stats.service.compilation;

import org.springframework.data.domain.Pageable;
import ru.practicum.stats.dto.compilation.CompilationDto;
import ru.practicum.stats.dto.compilation.NewCompilationDto;
import ru.practicum.stats.dto.compilation.UpdateCompilationRequest;

import java.util.List;

/**
 * Сервис для управления подборками событий.
 * Предоставляет операции создания, обновления, удаления и получения подборок.
 */
public interface CompilationService {

    /**
     * Сохраняет новую подборку.
     *
     * @param newCompilationDto данные новой подборки
     * @return созданная подборка
     */
    CompilationDto saveCompilation(NewCompilationDto newCompilationDto);

    /**
     * Удаляет подборку.
     *
     * @param compId идентификатор подборки
     * @throws ru.practicum.stats.exception.NotFoundException если подборка не найдена
     */
    void deleteCompilation(Long compId);

    /**
     * Обновляет существующую подборку.
     *
     * @param compId  идентификатор подборки
     * @param request данные для обновления
     * @return обновленная подборка
     * @throws ru.practicum.stats.exception.NotFoundException если подборка не найдена
     * @throws ru.practicum.stats.exception.ValidationException если заголовок превышает допустимую длину
     */
    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest request);

    /**
     * Возвращает список подборок с фильтром по закреплению и пагинацией.
     *
     * @param pinned   фильтр по закреплению (null - все)
     * @param pageable параметры пагинации
     * @return список подборок
     */
    List<CompilationDto> getCompilations(Boolean pinned, Pageable pageable);

    /**
     * Возвращает подборку по идентификатору.
     *
     * @param compId идентификатор подборки
     * @return подборка
     * @throws ru.practicum.stats.exception.NotFoundException если подборка не найдена
     */
    CompilationDto getCompilation(Long compId);
}