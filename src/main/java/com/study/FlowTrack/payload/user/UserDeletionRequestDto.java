package com.study.FlowTrack.payload.user;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDeletionRequestDto {
    private Long userIdToDelete;
}
