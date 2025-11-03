package com.study.FlowTrack.payload.user;

import com.study.FlowTrack.enums.ProjectRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserRoleUpdateRequestDto {
    @NotNull(message = "User ID to update must be provided.")
    private Long userIdToUpdate;

    @NotNull(message = "New role must be specified.")
    private ProjectRole role;
}
