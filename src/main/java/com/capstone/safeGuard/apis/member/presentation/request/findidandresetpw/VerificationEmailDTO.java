package com.capstone.safeGuard.apis.member.presentation.request.findidandresetpw;

import lombok.Data;

@Data
public class VerificationEmailDTO {
    private String inputId;
    private String inputCode;
}
