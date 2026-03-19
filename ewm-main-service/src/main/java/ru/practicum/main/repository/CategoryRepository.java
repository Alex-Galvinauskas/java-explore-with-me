package ru.practicum.main.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.main.model.Category;

import java.util.Optional;

@Repository
public interface CategoryRepository extends BaseRepository<Category> {

    Optional<Category> findByName(String name);
}