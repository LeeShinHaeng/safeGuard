package com.capstone.safeGuard.apis.member.presentation.request.findidandresetpw;

import jakarta.validation.constraints.Email;

public record FindMemberIdRequest(
	String name,

	@Email
	String email
) {
}
