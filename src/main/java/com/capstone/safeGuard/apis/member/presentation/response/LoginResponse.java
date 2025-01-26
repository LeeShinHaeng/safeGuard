package com.capstone.safeGuard.apis.member.presentation.response;

import lombok.Builder;

@Builder
public record LoginResponse(
	String authorization,
	String status,
	String type
) {
	public static LoginResponse of(String token, String status, String type) {
		return LoginResponse.builder()
			.authorization(token)
			.status(status)
			.type(type)
			.build();
	}
}
