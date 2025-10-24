package com.study.FlowTrack.payload.auth;

import lombok.Data;

@Data
public class AuthResponseDto {
    private String accessToken;

    public AuthResponseDto(String accessToken) {
        this.accessToken = accessToken;
    }
}