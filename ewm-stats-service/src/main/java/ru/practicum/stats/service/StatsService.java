package ru.practicum.stats.service;

import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.stats.exception.StatsValidationException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис для работы со статистикой посещений
 */
public interface StatsService {

    /**
     * Сохранить информацию о запросе к эндпоинту
     *
     * @param hitDto DTO с информацией о запросе
     * @return сохраненный EndpointHit с заполненным id
     */
    EndpointHit hit(EndpointHit hitDto);

    /**
     * Получить статистику по посещениям с фильтрацией по URI
     *
     * @param start  начало диапазона дат
     * @param end    конец диапазона дат
     * @param uris   список URI для фильтрации (может быть null)
     * @param unique флаг уникальности по IP
     * @return список статистики
     * @throws StatsValidationException если start позже end
     */
    List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);

    /**
     * Получить всю статистику без фильтрации по URI
     *
     * @param start  начало диапазона дат
     * @param end    конец диапазона дат
     * @param unique флаг уникальности по IP
     * @return список статистики
     */
    List<ViewStats> getStatsAll(LocalDateTime start, LocalDateTime end, boolean unique);
}