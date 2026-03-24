package ru.practicum.stats.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats.dto.compilation.CompilationDto;
import ru.practicum.stats.dto.compilation.NewCompilationDto;
import ru.practicum.stats.dto.compilation.UpdateCompilationRequest;
import ru.practicum.stats.service.compilation.CompilationService;

@Validated
@RestController
@RequestMapping(path = "/admin/compilations")
@RequiredArgsConstructor
@Tag(name = "Admin: Compilations", description = "Управление подборками событий (административный доступ)")
public class AdminCompilationController {

    private final CompilationService compilationService;

    @PostMapping
    @Operation(summary = "Создать новую подборку событий")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Подборка создана"),
            @ApiResponse(responseCode = "400",
                    description = "Некорректные данные, ошибка валидации, неверный формат запроса"),
            @ApiResponse(responseCode = "409", description = "Конфликт: подборка с таким названием уже существует")
    })
    public ResponseEntity<CompilationDto> saveCompilation(@Valid @RequestBody NewCompilationDto newCompilationDto) {
        CompilationDto compilation = compilationService.saveCompilation(newCompilationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(compilation);
    }

    @DeleteMapping("/{compId}")
    @Operation(summary = "Удалить подборку")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Подборка удалена"),
            @ApiResponse(responseCode = "400", description = "Некорректный ID подборки"),
            @ApiResponse(responseCode = "404", description = "Подборка не найдена")
    })
    public ResponseEntity<Void> deleteCompilation(
            @Parameter(description = "ID подборки", required = true, example = "1")
            @PathVariable @Positive Long compId) {
        compilationService.deleteCompilation(compId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{compId}")
    @Operation(summary = "Обновить подборку")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Подборка обновлена"),
            @ApiResponse(responseCode = "400",
                    description = "Некорректные данные, ошибка валидации, неверный тип параметра"),
            @ApiResponse(responseCode = "404", description = "Подборка не найдена"),
            @ApiResponse(responseCode = "409", description = "Конфликт: подборка с таким названием уже существует")
    })
    public ResponseEntity<CompilationDto> updateCompilation(
            @Parameter(description = "ID подборки", required = true, example = "1")
            @PathVariable @Positive Long compId,
            @Valid @RequestBody UpdateCompilationRequest request) {
        CompilationDto compilation = compilationService.updateCompilation(compId, request);
        return ResponseEntity.ok(compilation);
    }
}