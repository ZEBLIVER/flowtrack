package com.study.FlowTrack.payload.auth;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginDto {
    private String username;
    private String password;
}
