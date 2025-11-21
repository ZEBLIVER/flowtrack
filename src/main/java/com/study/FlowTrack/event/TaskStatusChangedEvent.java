package com.study.FlowTrack.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskStatusChangedEvent {
    private Long taskId;
    private Long projectId;
    private Long userId;
    private String oldStatus;
    private String newStatus;
    private Instant timestamp = Instant.now();
}
