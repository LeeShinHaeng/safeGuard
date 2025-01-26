package com.capstone.safeGuard.apis.member.presentation.response;

import lombok.Builder;

@Builder
public record MemberIdResponse(
	String status,
	String memberId
) {
	public static MemberIdResponse of(String status, String memberId) {
		return MemberIdResponse.builder().status(status).memberId(memberId).build();
	}
}
