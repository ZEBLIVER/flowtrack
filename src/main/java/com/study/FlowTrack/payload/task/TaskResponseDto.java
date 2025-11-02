package com.study.FlowTrack.payload.task;

import com.study.FlowTrack.enums.StatusTask;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TaskResponseDto {
    private Long id;
    private String title;
    private String description;
    private StatusTask statusTask;
    private String projectKey;
    private Long taskNumber;
}
