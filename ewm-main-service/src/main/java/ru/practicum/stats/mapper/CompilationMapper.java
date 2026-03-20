package ru.practicum.stats.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.practicum.stats.dto.compilation.CompilationDto;
import ru.practicum.stats.dto.compilation.NewCompilationDto;
import ru.practicum.stats.dto.compilation.UpdateCompilationRequest;
import ru.practicum.stats.model.Compilation;


@Mapper(componentModel = "spring", uses = {EventMapper.class})
public interface CompilationMapper {
    CompilationMapper INSTANCE = Mappers.getMapper(CompilationMapper.class);

    @Mapping(target = "events", source = "events")
    CompilationDto toDto(Compilation compilation);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    Compilation toEntity(NewCompilationDto newCompilationDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    Compilation updateFromRequest(UpdateCompilationRequest request, @org.mapstruct.MappingTarget Compilation compilation);
}