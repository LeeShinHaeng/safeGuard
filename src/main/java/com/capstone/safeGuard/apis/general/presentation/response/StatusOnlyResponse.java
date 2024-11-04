package com.capstone.safeGuard.apis.general.presentation.response;

import lombok.Builder;

@Builder
public record StatusOnlyResponse(
	int status
	) {
	public static StatusOnlyResponse of(int status) {
		return StatusOnlyResponse.builder()
			.status(status)
			.build();
	}
}
