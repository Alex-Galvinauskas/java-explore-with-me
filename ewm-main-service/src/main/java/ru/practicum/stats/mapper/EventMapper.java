package ru.practicum.stats.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.practicum.stats.dto.event.*;
import ru.practicum.stats.model.Event;


@Mapper(componentModel = "spring", uses = {CategoryMapper.class, UserMapper.class, LocationMapper.class})
public interface EventMapper {
    EventMapper INSTANCE = Mappers.getMapper(EventMapper.class);

    @Mapping(target = "category", source = "category")
    @Mapping(target = "initiator", source = "initiator")
    @Mapping(target = "views", source = "views")
    @Mapping(target = "createdOn", source = "createdOn", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Mapping(target = "publishedOn", source = "publishedOn", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Mapping(target = "eventDate", source = "eventDate", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    EventFullDto toFullDto(Event event);

    @Mapping(target = "category", source = "category")
    @Mapping(target = "initiator", source = "initiator")
    @Mapping(target = "views", source = "views")
    @Mapping(target = "eventDate", source = "eventDate", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    EventShortDto toShortDto(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "requests", ignore = true)
    @Mapping(target = "location", source = "location")
    Event toEntity(NewEventDto newEventDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "requests", ignore = true)
    @Mapping(target = "location", source = "location")
    Event updateFromUserRequest(UpdateEventUserRequest request, @org.mapstruct.MappingTarget Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "requests", ignore = true)
    @Mapping(target = "location", source = "location")
    Event updateFromAdminRequest(UpdateEventAdminRequest request, @org.mapstruct.MappingTarget Event event);
}