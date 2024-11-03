package com.capstone.safeGuard.apis.member.presentation.response;

import lombok.Builder;

public class LoginResponseToken {
    private String Authorization;

    @Builder
    public LoginResponseToken(String authorization) {
        Authorization = authorization;
    }
}
