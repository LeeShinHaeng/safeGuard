package com.capstone.safeGuard.apis.member.presentation.response;

import lombok.Builder;

import java.util.Map;

@Builder
public record ChildNameListResponse(
	Map<String, String> children
) {
	public static ChildNameListResponse fromMap(Map<String, String> children) {
		return ChildNameListResponse.builder()
			.children(children)
			.build();
	}
}
