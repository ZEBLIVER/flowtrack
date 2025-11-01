package com.study.FlowTrack.payload.user;

import com.study.FlowTrack.enums.ProjectRole;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserRoleUpdateRequestDto {
    private Long userIdToUpdate;
    private ProjectRole role;
}
