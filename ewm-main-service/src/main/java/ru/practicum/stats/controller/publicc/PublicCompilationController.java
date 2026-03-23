package ru.practicum.stats.controller.publicc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats.dto.compilation.CompilationDto;
import ru.practicum.stats.service.compilation.CompilationService;

import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/compilations")
@RequiredArgsConstructor
@Tag(name = "Public: Compilations", description = "Публичный API для работы с подборками событий")
public class PublicCompilationController {

    private final CompilationService compilationService;

    @GetMapping
    @Operation(summary = "Получить список подборок событий")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список подборок получен"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса")
    })
    public List<CompilationDto> getCompilations(
            @Parameter(description = "Закреплена ли подборка на главной странице")
            @RequestParam(required = false) Boolean pinned,
            @Parameter(description = "Индекс первого элемента", example = "0")
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @Parameter(description = "Количество элементов на странице", example = "10")
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);
        return compilationService.getCompilations(pinned, pageable);
    }

    @GetMapping("/{compId}")
    @Operation(summary = "Получить подборку событий по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Подборка найдена"),
            @ApiResponse(responseCode = "400", description = "Некорректный ID подборки"),
            @ApiResponse(responseCode = "404", description = "Подборка не найдена")
    })
    public CompilationDto getCompilation(
            @Parameter(description = "ID подборки", required = true, example = "1")
            @PathVariable @Positive Long compId) {
        return compilationService.getCompilation(compId);
    }
}