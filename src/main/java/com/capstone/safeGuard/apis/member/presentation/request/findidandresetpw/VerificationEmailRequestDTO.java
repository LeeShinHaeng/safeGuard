package com.capstone.safeGuard.apis.member.presentation.request.findidandresetpw;

import lombok.Getter;

@Getter
public class VerificationEmailRequestDTO {
    private String memberId;
    private boolean isMember; //member or child
}
