package ru.practicum.main.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import ru.practicum.main.model.Compilation;

@Repository
public interface CompilationRepository extends BaseRepository<Compilation> {

    Page<Compilation> findByPinned(Boolean pinned, Pageable pageable);
}