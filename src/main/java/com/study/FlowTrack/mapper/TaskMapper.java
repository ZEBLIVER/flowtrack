package com.study.FlowTrack.mapper;

import com.study.FlowTrack.model.Task;
import com.study.FlowTrack.payload.task.TaskCreationDto;
import com.study.FlowTrack.payload.task.TaskResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface TaskMapper {
    Task toEntity(TaskCreationDto dto);

    @Mapping(source = "project.key", target = "projectKey")
    @Mapping(source = "taskNumber", target = "taskNumber")
    @Mapping(source = "statusTaskEntity.statusTask", target = "statusTask")
    TaskResponseDto toResponseDto(Task task);

    List<TaskResponseDto> toResponseListDto(List<Task> tasks);
}
