package ru.practicum.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import ru.practicum.main.model.BaseEntity;
import java.util.List;

@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity> extends JpaRepository<T, Long> {

    default <S extends T> S saveEntity(S entity) {
        return save(entity);
    }

    default List<T> findAllEntities() {
        return findAll();
    }

    default boolean existsEntityById(Long id) {
        return existsById(id);
    }

    default void deleteEntity(T entity) {
        delete(entity);
    }
}