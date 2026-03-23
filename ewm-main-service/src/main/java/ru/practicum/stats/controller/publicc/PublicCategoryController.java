package ru.practicum.stats.controller.publicc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats.dto.category.CategoryDto;
import ru.practicum.stats.service.category.CategoryService;

import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/categories")
@RequiredArgsConstructor
@Tag(name = "Public: Categories", description = "Публичный API для работы с категориями")
public class PublicCategoryController {
    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Получить список категорий")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список категорий получен"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса")
    })
    public List<CategoryDto> getCategories(
            @Parameter(description = "Индекс первого элемента", example = "0")
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @Parameter(description = "Количество элементов на странице", example = "10")
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        return categoryService.getCategories(from, size);
    }

    @GetMapping("/{catId}")
    @Operation(summary = "Получить категорию по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Категория найдена"),
            @ApiResponse(responseCode = "400", description = "Некорректный ID категории"),
            @ApiResponse(responseCode = "404", description = "Категория не найдена")
    })
    public CategoryDto getCategory(
            @Parameter(description = "ID категории", required = true, example = "1")
            @PathVariable @Positive Long catId) {
        return categoryService.getCategory(catId);
    }
}