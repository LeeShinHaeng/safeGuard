package com.capstone.safeGuard.apis.member.presentation.request.signupandlogin;

import lombok.Data;

@Data
public class ChildSignUpRequestDTO {
    private String memberId;
    private String childPassword;
    private String childName;


    public CharSequence getChild_password() {
        return childPassword;
    }
}
