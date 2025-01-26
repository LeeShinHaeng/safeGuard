package com.capstone.safeGuard.apis.map.presentation.response;

import lombok.Builder;

@Builder
public record AreaDetailIdResponse(
	String id,
	AreaDetailResponse data
) {
	public static AreaDetailIdResponse of(String id, AreaDetailResponse data) {
		return AreaDetailIdResponse.builder()
			.id(id)
			.data(data)
			.build();
	}
}
