package com.study.FlowTrack.payload.auth;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegistrationDto {
    private String username;
    private String password;
}
