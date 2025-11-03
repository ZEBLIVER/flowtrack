package com.study.FlowTrack.payload.project;

import com.study.FlowTrack.enums.ProjectRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectMembershipRequestDto {
    @NotNull(message = "User ID to add must be provided.")
    private Long userIdToAdd;

    @NotNull(message = "Project role must be specified.")
    private ProjectRole projectRole;
}
