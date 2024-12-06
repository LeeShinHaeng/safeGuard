package com.capstone.safeGuard.apis.map.presentation.response;

import lombok.Builder;

@Builder
public record AreaPersistResponse(
	int status,
	String areaId
) {
	public static AreaPersistResponse of(int status, String areaId) {
		return AreaPersistResponse.builder()
			.status(status)
			.areaId(areaId)
			.build();
	}
}
