package ru.practicum.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.stats.model.EndpointHitEntity;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<EndpointHitEntity, Long> {

    /**
     * Получить статистику по посещениям с фильтрацией по URI
     *
     * @param start  начало диапазона дат
     * @param end    конец диапазона дат
     * @param uris   список URI для фильтрации
     * @param unique флаг уникальности по IP (true - уникальные IP, false - все запросы)
     * @return список объектов ViewStats с агрегированной статистикой, отсортированный по убыванию количества запросов
     */
    @Query("SELECT new ru.practicum.stats.dto.ViewStats(" +
            "h.app, " +
            "h.uri, " +
            "CASE WHEN :unique = true THEN COUNT(DISTINCT h.ip) ELSE COUNT(h.ip) END) " +
            "FROM EndpointHitEntity h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "AND h.uri IN :uris " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(h.ip) DESC")
    List<ViewStats> getStats(@Param("start") LocalDateTime start,
                             @Param("end") LocalDateTime end,
                             @Param("uris") List<String> uris,
                             @Param("unique") boolean unique);

    /**
     * Получить всю статистику по посещениям без фильтрации по URI
     *
     * @param start  начало диапазона дат
     * @param end    конец диапазона дат
     * @param unique флаг уникальности по IP (true - уникальные IP, false - все запросы)
     * @return список объектов ViewStats с агрегированной статистикой, отсортированный по убыванию количества запросов
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