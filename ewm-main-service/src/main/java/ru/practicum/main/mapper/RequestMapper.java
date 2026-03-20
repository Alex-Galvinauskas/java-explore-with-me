package ru.practicum.main.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.practicum.main.dto.request.ParticipationRequestDto;
import ru.practicum.main.model.Request;

@Mapper(componentModel = "spring")
public interface RequestMapper {
    RequestMapper INSTANCE = Mappers.getMapper(RequestMapper.class);

    @Mapping(target = "event", source = "event.id")
    @Mapping(target = "requester", source = "requester.id")
    @Mapping(target = "created", source = "created", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    ParticipationRequestDto toDto(Request request);
}