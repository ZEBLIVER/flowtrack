package com.study.FlowTrack.payload.task;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TaskUpdateDto {
    @Size(min = 5, max = 100, message = "Task title must be between 5 and 100 characters.")
    private String title;

    @Size(max = 1000, message = "Task description cannot exceed 1000 characters.")
    private String description;

    @NotNull(message = "Assigned User ID cannot be null.")
    private Long assignedUserId;

    @NotNull(message = "New Status ID cannot be null.")
    private Long newStatusId;
}
