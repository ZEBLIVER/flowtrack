package com.study.FlowTrack.payload.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDeletionRequestDto {
    @NotNull(message = "User ID to delete must be provided.")
    private Long userIdToDelete;
}
