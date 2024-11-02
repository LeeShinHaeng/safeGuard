package com.capstone.safeGuard.apis.member.presentation.request.signupandlogin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignUpRequestDTO {
    @NotBlank
    private String inputID;
    @NotBlank
    private String inputName;
    @NotBlank
    @Email
    private String inputEmail;
    @NotBlank
    private String inputPW;
    private String fcmToken;
    public String toString(){
        return inputID +inputName +"가입 완료!";
    }
}
