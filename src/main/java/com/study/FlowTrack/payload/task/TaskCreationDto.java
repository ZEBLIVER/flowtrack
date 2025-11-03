package com.study.FlowTrack.payload.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TaskCreationDto {
    @NotBlank(message = "Task title is required and cannot be empty.")
    @Size(min = 4, max = 100, message = "Task title must be between 4 and 100 characters.")
    private String title;

    @Size(max = 1000, message = "Task description cannot exceed 1000 characters.")
    private String description;

    @NotNull(message = "Project ID is required for task creation.")
    private Long projectId;
}
