package ru.practicum.stats.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.stats.exception.BadRequestException;
import ru.practicum.stats.exception.StatsValidationException;
import ru.practicum.stats.mapper.StatsMapper;
import ru.practicum.stats.model.EndpointHitEntity;
import ru.practicum.stats.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Реализация сервиса статистики
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;
    private final StatsMapper statsMapper;

    @Override
    @Transactional
    public void hit(EndpointHit hitDto) {
        log.info("Сохранение информации о запросе: {}", hitDto);

        validateHitDto(hitDto);

        EndpointHitEntity entity = statsMapper.toEntity(hitDto);

        if (entity.getTimestamp() == null) {
            entity.setTimestamp(LocalDateTime.now());
            log.debug("Timestamp не указан, установлено текущее время: {}", entity.getTimestamp());
        }

        EndpointHitEntity savedEntity = statsRepository.save(entity);
        log.info("Информация о запросе успешно сохранена с id: {}", savedEntity.getId());
    }


    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        log.info("Запрос статистики: start={}, end={}, uris={}, unique={}", start, end, uris, unique);

        validateDates(start, end);

        List<String> uriList = (uris != null && uris.isEmpty()) ? null : uris;

        List<ViewStats> stats = statsRepository.getStats(start, end, uriList, unique);

        log.info("Получено {} записей статистики", stats.size());
        return stats;
    }

    @Override
    public List<ViewStats> getStatsAll(LocalDateTime start, LocalDateTime end, boolean unique) {
        log.info("Запрос всей статистики: start={}, end={}, unique={}", start, end, unique);

        validateDates(start, end);

        List<ViewStats> stats = statsRepository.getStatsAll(start, end, unique);

        log.info("Получено {} записей статистики", stats.size());
        return stats;
    }

    /**
     * Валидация диапазона дат
     * @param start начало диапазона
     * @param end конец диапазона
     * @throws StatsValidationException если start позже end
     */
    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new StatsValidationException(
                    "Даты начала и конца должны быть указаны",
                    "Отсутствуют обязательные параметры дат"
            );
        }

        if (start.isAfter(end)) {
            throw new StatsValidationException(
                    String.format("Дата начала (%s) не может быть позже даты конца (%s)", start, end),
                    "Некорректный диапазон дат"
            );
        }
    }

    private void validateHitDto(EndpointHit hitDto) {
        if (hitDto.getApp() == null || hitDto.getApp().isBlank()) {
            throw new BadRequestException("Поле 'app' не может быть пустым");
        }
        if (hitDto.getUri() == null || hitDto.getUri().isBlank()) {
            throw new BadRequestException("Поле 'uri' не может быть пустым");
        }
        if (hitDto.getIp() == null || hitDto.getIp().isBlank()) {
            throw new BadRequestException("Поле 'ip' не может быть пустым");
        }
    }
}