package ru.practicum.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.main.dto.ViewStats;
import ru.practicum.main.model.EndpointHitEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Репозиторий для работы со статистикой посещений
 */
@Repository
public interface StatsRepository extends JpaRepository<EndpointHitEntity, Long> {

    /**
     * Получение статистики по посещениям с возможностью фильтрации
     * @param start начало диапазона дат
     * @param end конец диапазона дат
     * @param uris список URI для фильтрации (может быть null или пустым)
     * @param unique флаг уникальности по IP
     * @return список статистики ViewStats
     */
    @Query("SELECT new ru.practicum.stats.dto.ViewStats(" +
            "h.app, " +
            "h.uri, " +
            "CASE WHEN :unique = true THEN COUNT(DISTINCT h.ip) ELSE COUNT(h.ip) END) " +
            "FROM EndpointHitEntity h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "AND (:uris IS NULL OR h.uri IN :uris) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(h.ip) DESC")
    List<ViewStats> getStats(@Param("start") LocalDateTime start,
                             @Param("end") LocalDateTime end,
                             @Param("uris") List<String> uris,
                             @Param("unique") boolean unique);

    /**
     * Метод для получения статистики без фильтрации по URI
     */
    @Query("SELECT new ru.practicum.stats.dto.ViewStats(" +
            "h.app, " +
            "h.uri, " +
            "CASE WHEN :unique = true THEN COUNT(DISTINCT h.ip) ELSE COUNT(h.ip) END) " +
            "FROM EndpointHitEntity h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(h.ip) DESC")
    List<ViewStats> getStatsAll(@Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end,
                                @Param("unique") boolean unique);

}