package com.capstone.safeGuard.apis.member.presentation.request.signupandlogin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignUpRequest(
	@NotBlank
	String inputID,

	@NotBlank
	String inputName,

	@NotBlank
	@Email
	String inputEmail,

	@NotBlank
	String inputPW,

	String fcmToken
) {
	public String toString() {
		return inputID + inputName + "가입 완료!";
	}
}
