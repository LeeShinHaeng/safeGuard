package com.capstone.safeGuard.apis.member.presentation.request.signupandlogin;

public record UpdateMemberNameRequest(
	String userID,
	String nickname
) {

}
