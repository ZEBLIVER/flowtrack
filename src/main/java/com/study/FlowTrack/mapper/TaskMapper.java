package com.study.FlowTrack.mapper;

import com.study.FlowTrack.model.Task;
import com.study.FlowTrack.payload.task.TaskCreationDto;
import com.study.FlowTrack.payload.task.TaskResponseDto;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface TaskMapper {
    Task toEntity(TaskCreationDto dto);

    TaskResponseDto toResponseDto(Task task);
}
