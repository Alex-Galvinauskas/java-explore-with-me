package ru.practicum.stats.service.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.dto.category.CategoryDto;
import ru.practicum.stats.dto.category.NewCategoryDto;
import ru.practicum.stats.exception.ConflictException;
import ru.practicum.stats.exception.NotFoundException;
import ru.practicum.stats.mapper.CategoryMapper;
import ru.practicum.stats.model.Category;
import ru.practicum.stats.repository.CategoryRepository;
import ru.practicum.stats.repository.EventRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        log.info("Добавление новой категории: {}", newCategoryDto.getName());

        // ✅ Добавляем предварительную проверку
        categoryRepository.findByName(newCategoryDto.getName())
                .ifPresent(category -> {
                    throw new ConflictException(
                            String.format("Категория с именем '%s' уже существует", newCategoryDto.getName())
                    );
                });

        Category category = categoryMapper.toEntity(newCategoryDto);

        try {
            Category savedCategory = categoryRepository.save(category);
            log.info("Категория успешно добавлена с id: {}", savedCategory.getId());
            return categoryMapper.toDto(savedCategory);
        } catch (DataIntegrityViolationException e) {
            String message = String.format("Категория с именем '%s' уже существует", newCategoryDto.getName());
            log.warn(message, e);
            throw new ConflictException(message);
        }
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        log.info("Обновление категории с id: {}", catId);

        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с id=" + catId + " не найдена"));

        if (category.getName().equals(categoryDto.getName())) {
            log.info("Имя категории не изменилось");
            return categoryMapper.toDto(category);
        }

        // ✅ Проверяем, что новое имя не занято другой категорией
        categoryRepository.findByName(categoryDto.getName())
                .ifPresent(existingCategory -> {
                    if (!existingCategory.getId().equals(catId)) {
                        throw new ConflictException(
                                String.format("Категория с именем '%s' уже существует", categoryDto.getName())
                        );
                    }
                });

        category.setName(categoryDto.getName());

        try {
            Category updatedCategory = categoryRepository.save(category);
            log.info("Категория с id: {} успешно обновлена", catId);
            return categoryMapper.toDto(updatedCategory);
        } catch (DataIntegrityViolationException e) {
            String message = String.format("Категория с именем '%s' уже существует", categoryDto.getName());
            log.warn(message, e);
            throw new ConflictException(message);
        }
    }

    @Override
    @Transactional
    public void deleteCategory(Long catId) {
        log.info("Удаление категории с id: {}", catId);

        if (!categoryRepository.existsById(catId)) {
            throw new NotFoundException("Категория с id=" + catId + " не найдена");
        }

        if (eventRepository.existsByCategoryId(catId)) {
            String message = String.format("Невозможно удалить категорию с id=%d," +
                    " так как с ней связаны существующие события", catId);
            log.warn(message);
            throw new ConflictException(message);
        }

        try {
            categoryRepository.deleteById(catId);
            log.info("Категория с id: {} успешно удалена", catId);
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка при удалении категории с id: {}", catId, e);
            throw new ConflictException("Невозможно удалить категорию с id=" + catId +
                    ", так как с ней связаны события");
        }
    }

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        log.info("Получение списка категорий: from={}, size={}", from, size);

        Pageable pageable = PageRequest.of(from / size, size);
        List<Category> categories = categoryRepository.findAll(pageable).getContent();

        return categories.stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategory(Long catId) {
        log.info("Получение категории с id: {}", catId);

        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с id=" + catId + " не найдена"));

        return categoryMapper.toDto(category);
    }
}