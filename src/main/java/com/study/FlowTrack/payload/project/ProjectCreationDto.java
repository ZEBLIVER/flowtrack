package com.study.FlowTrack.payload.project;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectCreationDto {
    private String name;
    private String key;
    private String description;
    //добавить валидацию
}
