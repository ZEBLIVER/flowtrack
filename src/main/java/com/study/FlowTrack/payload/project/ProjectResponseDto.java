package com.study.FlowTrack.payload.project;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ProjectResponseDto {
    private Long id;
    private String name;
    private String key;
    private LocalDateTime createdAt;
}
