package com.capstone.safeGuard.apis.member.presentation.response;

import lombok.Builder;

@Builder
public record ChildResponse(
	String id,
	String childName,
	String lastStatus
) {
	public static ChildResponse of(String id, String name, String lastStatus) {
		return ChildResponse.builder()
			.id(id)
			.childName(name)
			.lastStatus(lastStatus)
			.build();
	}
}
