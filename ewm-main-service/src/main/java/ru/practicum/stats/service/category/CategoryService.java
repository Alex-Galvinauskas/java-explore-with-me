package ru.practicum.stats.service.category;

import ru.practicum.stats.dto.category.CategoryDto;
import ru.practicum.stats.dto.category.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto addCategory(NewCategoryDto newCategoryDto);

    CategoryDto updateCategory(Long catId, CategoryDto categoryDto);

    void deleteCategory(Long catId);

    List<CategoryDto> getCategories(int from, int size);

    CategoryDto getCategory(Long catId);
}