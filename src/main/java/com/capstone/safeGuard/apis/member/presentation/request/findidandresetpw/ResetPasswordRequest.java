package com.capstone.safeGuard.apis.member.presentation.request.findidandresetpw;

public record ResetPasswordRequest(
	String id,
	String newPassword
) {
}
