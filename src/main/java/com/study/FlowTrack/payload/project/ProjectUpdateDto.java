package com.study.FlowTrack.payload.project;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectUpdateDto {
    private String name;
    private String description;
}
