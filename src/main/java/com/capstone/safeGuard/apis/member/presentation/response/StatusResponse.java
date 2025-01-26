package com.capstone.safeGuard.apis.member.presentation.response;

import lombok.Builder;

@Builder
public record StatusResponse(
	String status
) {
	public static StatusResponse of(String status) {
		return StatusResponse.builder().status(status).build();
	}
}
