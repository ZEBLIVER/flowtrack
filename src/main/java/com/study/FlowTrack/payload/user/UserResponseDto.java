package com.study.FlowTrack.payload.user;

import com.study.FlowTrack.enums.ProjectRole;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserResponseDto {
    private Long id;
    private String name;
    private ProjectRole projectRole;
}
