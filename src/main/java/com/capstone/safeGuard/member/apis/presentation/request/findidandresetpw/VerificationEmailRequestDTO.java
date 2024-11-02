package com.capstone.safeGuard.member.apis.presentation.request.findidandresetpw;

import lombok.Getter;

@Getter
public class VerificationEmailRequestDTO {
    private String memberId;
    private boolean isMember; //member or child
}
