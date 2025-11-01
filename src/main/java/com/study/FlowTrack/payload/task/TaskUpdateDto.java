package com.study.FlowTrack.payload.task;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TaskUpdateDto {
    private String title;
    private String description;
    private Long assignedUserId;
    private Long newStatusId;
}
