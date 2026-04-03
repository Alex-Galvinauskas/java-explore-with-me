package ru.practicum.stats.mapper;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.stats.dto.comment.CommentDto;
import ru.practicum.stats.dto.comment.NewCommentDto;
import ru.practicum.stats.dto.comment.UpdateCommentDto;
import ru.practicum.stats.model.Comment;
import ru.practicum.stats.model.Event;
import ru.practicum.stats.model.User;
import ru.practicum.stats.repository.EventRepository;
import ru.practicum.stats.repository.UserRepository;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class CommentMapper {

    @Autowired
    protected EventRepository eventRepository;

    @Autowired
    protected UserRepository userRepository;

    @Mappings({
            @Mapping(target = "eventId", source = "event.id"),
            @Mapping(target = "eventTitle", source = "event.title"),
            @Mapping(target = "authorId", source = "author.id"),
            @Mapping(target = "authorName", source = "author.name"),
            @Mapping(target = "status", source = "status"),
            @Mapping(target = "edited", source = "edited")
    })
    public abstract CommentDto toDto(Comment comment);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdOn", expression = "java(java.time.LocalDateTime.now())"),
            @Mapping(target = "updatedOn", ignore = true),
            @Mapping(target = "edited", constant = "false"),
            @Mapping(target = "event", source = "eventId", qualifiedByName = "eventById"),
            @Mapping(target = "author", source = "authorId", qualifiedByName = "userById"),
            @Mapping(target = "status", constant = "PENDING")
    })
    public abstract Comment toNewEntity(NewCommentDto dto, Long eventId, Long authorId);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdOn", ignore = true),
            @Mapping(target = "updatedOn", expression = "java(java.time.LocalDateTime.now())"),
            @Mapping(target = "edited", constant = "true"),
            @Mapping(target = "event", ignore = true),
            @Mapping(target = "author", ignore = true),
            @Mapping(target = "status", ignore = true)
    })
    public abstract void updateEntityFromDto(UpdateCommentDto dto, @MappingTarget Comment comment);

    @Named("eventById")
    protected Event eventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + id));
    }

    @Named("userById")
    protected User userById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }
}