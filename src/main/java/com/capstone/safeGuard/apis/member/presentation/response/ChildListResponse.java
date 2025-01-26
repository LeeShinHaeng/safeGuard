package com.capstone.safeGuard.apis.member.presentation.response;

import lombok.Builder;

import java.util.List;

@Builder
public record ChildListResponse(
	List<ChildResponse> childList
) {
	public static ChildListResponse fromChildList(List<ChildResponse> childList) {
		return ChildListResponse.builder()
			.childList(childList)
			.build();
	}
}
