package com.study.FlowTrack.mapper;

import com.study.FlowTrack.model.Project;
import com.study.FlowTrack.model.ProjectMembership;
import com.study.FlowTrack.model.Task;
import com.study.FlowTrack.model.User;
import com.study.FlowTrack.payload.project.ProjectCreationDto;
import com.study.FlowTrack.payload.project.ProjectResponseDto;
import com.study.FlowTrack.payload.task.TaskResponseDto;
import com.study.FlowTrack.payload.user.UserResponseDto;
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

    @Mapping(source = "user.name", target = "name") // Получаем name из вложенного объекта User
    @Mapping(source = "projectRole", target = "projectRole") // Получаем projectRole напрямую
    UserResponseDto toResponseUserDto(ProjectMembership membership);

    TaskResponseDto toTaskResponseDto(Task task);

    List<TaskResponseDto> toTaskResponseDtoList(List<Task> tasks);
}
