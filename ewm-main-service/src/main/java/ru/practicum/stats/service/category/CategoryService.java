package ru.practicum.stats.service.category;

import ru.practicum.stats.dto.category.CategoryDto;
import ru.practicum.stats.dto.category.NewCategoryDto;

import java.util.List;

/**
 * Сервис для управления категориями событий.
 * Предоставляет операции создания, обновления, удаления и получения категорий.
 */
public interface CategoryService {

    /**
     * Добавляет новую категорию.
     *
     * @param newCategoryDto данные новой категории
     * @return созданная категория
     * @throws ru.practicum.stats.exception.ConflictException если категория с таким именем уже существует
     */
    CategoryDto addCategory(NewCategoryDto newCategoryDto);

    /**
     * Обновляет существующую категорию.
     *
     * @param catId        идентификатор категории
     * @param categoryDto  обновленные данные категории
     * @return обновленная категория
     * @throws ru.practicum.stats.exception.NotFoundException если категория не найдена
     * @throws ru.practicum.stats.exception.ConflictException если новое имя уже занято другой категорией
     */
    CategoryDto updateCategory(Long catId, CategoryDto categoryDto);

    /**
     * Удаляет категорию.
     *
     * @param catId идентификатор категории
     * @throws ru.practicum.stats.exception.NotFoundException если категория не найдена
     * @throws ru.practicum.stats.exception.ConflictException если существуют события, связанные с категорией
     */
    void deleteCategory(Long catId);

    /**
     * Возвращает список категорий с пагинацией.
     *
     * @param from начальная позиция
     * @param size количество элементов
     * @return список категорий
     */
    List<CategoryDto> getCategories(int from, int size);

    /**
     * Возвращает категорию по идентификатору.
     *
     * @param catId идентификатор категории
     * @return категория
     * @throws ru.practicum.stats.exception.NotFoundException если категория не найдена
     */
    CategoryDto getCategory(Long catId);
}