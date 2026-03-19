package ru.practicum.main.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import ru.practicum.main.dto.event.LocationDto;
import ru.practicum.main.model.Location;


@Mapper(componentModel = "spring")
public interface LocationMapper {
    LocationMapper INSTANCE = Mappers.getMapper(LocationMapper.class);
    GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    LocationDto toDto(Location location);

    @Mapping(target = "geoPoint", expression = "java(toPoint(locationDto))")
    Location toEntity(LocationDto locationDto);

    default Point toPoint(LocationDto locationDto) {
        if (locationDto == null || locationDto.getLat() == null || locationDto.getLon() == null) {
            return null;
        }
        return GEOMETRY_FACTORY.createPoint(new Coordinate(locationDto.getLon(),
                locationDto.getLat()));
    }

    default LocationDto fromPoint(Point point) {
        if (point == null) {
            return null;
        }
        return LocationDto.builder()
                .lat((float) point.getY())
                .lon((float) point.getX())
                .build();
    }
}