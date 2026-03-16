package ru.practicum.stats.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.practicum.stats.dto.user.NewUserRequest;
import ru.practicum.stats.dto.user.UserDto;
import ru.practicum.stats.dto.user.UserShortDto;
import ru.practicum.stats.model.User;


@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserDto toDto(User user);

    UserShortDto toShortDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    @Mapping(target = "requests", ignore = true)
    User toEntity(NewUserRequest newUserRequest);
}