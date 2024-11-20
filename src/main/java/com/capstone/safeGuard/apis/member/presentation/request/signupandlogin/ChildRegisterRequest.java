package com.capstone.safeGuard.apis.member.presentation.request.signupandlogin;

public record ChildRegisterRequest(
	String memberId,
	String childPassword,
	String childName
) {
}
