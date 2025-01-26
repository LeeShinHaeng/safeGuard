package com.capstone.safeGuard.apis.member.presentation.response;

import lombok.Builder;

import java.util.Map;

@Builder
public record ParentingListResponse(
	Map<String, String> parenting
) {
	public static ParentingListResponse from(Map<String, String> parenting) {
		return ParentingListResponse.builder()
			.parenting(parenting)
			.build();
	}
}
