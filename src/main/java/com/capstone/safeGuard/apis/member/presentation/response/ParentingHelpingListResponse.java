package com.capstone.safeGuard.apis.member.presentation.response;

import lombok.Builder;

import java.util.Map;

@Builder
public record ParentingHelpingListResponse(
	Map<String, String> parenting,
	Map<String, String> helping
) {
	public static ParentingHelpingListResponse from(Map<String, String> parenting, Map<String, String> helping) {
		return ParentingHelpingListResponse.builder()
			.parenting(parenting)
			.helping(helping)
			.build();
	}
}
