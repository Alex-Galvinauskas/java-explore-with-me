package ru.practicum.stats.validation.compilation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.stats.dto.compilation.NewCompilationDto;
import ru.practicum.stats.exception.NotFoundException;
import ru.practicum.stats.exception.ValidationException;
import ru.practicum.stats.model.Compilation;
import ru.practicum.stats.repository.CompilationRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompilationValidator {

    private final CompilationRepository compilationRepository;

    public Compilation validateAndGetCompilation(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id " + compId + " не найдена"));
    }

    public void validateCompilationExists(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Подборка с id " + compId + " не найдена");
        }
    }

    public void validateTitleLength(String title) {
        if (title != null && title.length() > 50) {
            throw new ValidationException("Длина title не должна превышать 50 символов");
        }
    }

    public NewCompilationDto prepareNewCompilationDto(NewCompilationDto dto) {
        if (dto.getPinned() == null) {
            dto.setPinned(false);
        }
        return dto;
    }
}