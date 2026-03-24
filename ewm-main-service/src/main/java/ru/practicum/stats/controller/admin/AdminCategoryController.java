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
import ru.practicum.stats.dto.category.CategoryDto;
import ru.practicum.stats.dto.category.NewCategoryDto;
import ru.practicum.stats.service.category.CategoryService;

@Validated
@RestController
@RequestMapping(path = "/admin/categories")
@RequiredArgsConstructor
@Tag(name = "Admin: Categories", description = "Управление категориями (административный доступ)")
public class AdminCategoryController {
    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "Добавить новую категорию")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Категория создана"),
            @ApiResponse(responseCode = "400",
                    description = "Некорректные данные, ошибка валидации, неверный формат запроса"),
            @ApiResponse(responseCode = "409", description = "Конфликт: категория с таким именем уже существует")
    })
    public ResponseEntity<CategoryDto> addCategory(@Valid @RequestBody NewCategoryDto newCategoryDto) {
        CategoryDto category = categoryService.addCategory(newCategoryDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @PatchMapping("/{catId}")
    @Operation(summary = "Обновить категорию")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Категория обновлена"),
            @ApiResponse(responseCode = "400",
                    description = "Некорректные данные, ошибка валидации, неверный тип параметра"),
            @ApiResponse(responseCode = "404", description = "Категория не найдена"),
            @ApiResponse(responseCode = "409", description = "Конфликт: категория с таким именем уже существует")
    })
    public ResponseEntity<CategoryDto> updateCategory(
            @Parameter(description = "ID категории", required = true, example = "1")
            @PathVariable @Positive Long catId,
            @Valid @RequestBody CategoryDto categoryDto) {
        CategoryDto category = categoryService.updateCategory(catId, categoryDto);
        return ResponseEntity.ok(category);
    }

    @DeleteMapping("/{catId}")
    @Operation(summary = "Удалить категорию")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Категория удалена"),
            @ApiResponse(responseCode = "400", description = "Некорректный ID категории"),
            @ApiResponse(responseCode = "404", description = "Категория не найдена"),
            @ApiResponse(responseCode = "409", description = "Конфликт: категория содержит события")
    })
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "ID категории", required = true, example = "1")
            @PathVariable @Positive Long catId) {
        categoryService.deleteCategory(catId);
        return ResponseEntity.noContent().build();
    }
}