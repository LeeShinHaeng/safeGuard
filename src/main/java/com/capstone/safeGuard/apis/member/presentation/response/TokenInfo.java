package com.capstone.safeGuard.apis.member.presentation.response;

import lombok.Builder;

@Builder
public record TokenInfo(
	String grantType,
	String accessToken,
	String refreshToken
) {
}