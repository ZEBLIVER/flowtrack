package com.study.FlowTrack.payload.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectUpdateDto {
    @NotBlank(message = "Project name is required and cannot be empty.")
    @Size(min = 3, max = 30, message = "Project name must be between 3 and 30 characters.")
    private String name;

    @Size(max = 300, message = "Project description cannot exceed 300 characters.")
    private String description;
}
