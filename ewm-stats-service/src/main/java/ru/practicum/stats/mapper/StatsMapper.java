package ru.practicum.stats.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.model.EndpointHitEntity;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        implementationPackage = "ru.practicum.stats.mapper.impl")
public interface StatsMapper {

    @Mapping(target = "id", ignore = true)
    EndpointHitEntity toEntity(EndpointHit dto);

    EndpointHit toDto(EndpointHitEntity entity);
}