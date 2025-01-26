package com.capstone.safeGuard.apis.map.presentation.response;

import lombok.Builder;

import java.util.Map;

@Builder
public record AreaListResponse(
	Map<String, ReadAreaResponse> data
) {
	public static AreaListResponse from(Map<String, ReadAreaResponse> data) {
		return AreaListResponse.builder()
			.data(data)
			.build();
	}
}
