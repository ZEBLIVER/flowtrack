package com.study.FlowTrack.mapper;

import com.study.FlowTrack.model.Task;
import com.study.FlowTrack.payload.task.TaskCreationDto;
import com.study.FlowTrack.payload.task.TaskResponseDto;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface TaskMapper {
    Task toEntity(TaskCreationDto dto);

    TaskResponseDto toResponseDto(Task task);

    List<TaskResponseDto> toResponseListDto(List<Task> tasks);
}
