package ru.practicum.main.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.practicum.main.dto.user.NewUserRequest;
import ru.practicum.main.dto.user.UserDto;
import ru.practicum.main.dto.user.UserShortDto;
import ru.practicum.main.model.User;


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