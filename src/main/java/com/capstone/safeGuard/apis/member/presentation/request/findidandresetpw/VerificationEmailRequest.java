package com.capstone.safeGuard.apis.member.presentation.request.findidandresetpw;

public record VerificationEmailRequest(
	String inputId,
	String inputCode
) {
}
