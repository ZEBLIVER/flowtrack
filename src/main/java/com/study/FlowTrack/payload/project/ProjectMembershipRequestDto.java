package com.study.FlowTrack.payload.project;

import com.study.FlowTrack.enums.ProjectRole;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectMembershipRequestDto {
    private Long userIdToAdd;
    private ProjectRole projectRole;
}
