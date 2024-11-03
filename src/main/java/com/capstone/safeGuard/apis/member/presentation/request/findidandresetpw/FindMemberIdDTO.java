package com.capstone.safeGuard.apis.member.presentation.request.findidandresetpw;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class FindMemberIdDTO {
    private String name;
    @Email
    private String email;
}
