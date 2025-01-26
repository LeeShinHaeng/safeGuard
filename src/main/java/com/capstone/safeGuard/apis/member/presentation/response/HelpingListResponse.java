package com.capstone.safeGuard.apis.member.presentation.response;

import lombok.Builder;

import java.util.Map;

@Builder
public record HelpingListResponse(
	Map<String, String> helping
) {
	public static HelpingListResponse from(Map<String, String> helping) {
		return HelpingListResponse.builder()
			.helping(helping)
			.build();
	}
}
