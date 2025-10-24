package com.study.FlowTrack.mapper;

import com.study.FlowTrack.model.Project;
import com.study.FlowTrack.payload.project.ProjectCreationDto;
import com.study.FlowTrack.payload.project.ProjectResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface ProjectMapper {
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Project toEntity(ProjectCreationDto dto);

    ProjectResponseDto toResponseDto(Project project);

    List<ProjectResponseDto> toResponseDtoList(List<Project> projects);
}
