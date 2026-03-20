package ru.practicum.stats.service.compilation;

import org.springframework.data.domain.Pageable;
import ru.practicum.stats.dto.compilation.CompilationDto;
import ru.practicum.stats.dto.compilation.NewCompilationDto;
import ru.practicum.stats.dto.compilation.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    CompilationDto saveCompilation(NewCompilationDto newCompilationDto);

    void deleteCompilation(Long compId);

    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest request);

    List<CompilationDto> getCompilations(Boolean pinned, Pageable pageable);

    CompilationDto getCompilation(Long compId);
}