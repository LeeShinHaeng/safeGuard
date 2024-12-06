package com.capstone.safeGuard.apis.member.presentation.request.signupandlogin;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
	@NotBlank
	String editTextID,
	@NotBlank
	String editTextPW,
	@NotBlank
	String loginType,

	String fcmToken
) {
}
