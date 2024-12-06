package com.capstone.safeGuard.apis.file.presentation.response;

import lombok.Builder;

@Builder
public record FilePersistResponse (
	int status,
	String filePath
) {
	public static FilePersistResponse of(int status, String filePath) {
		return FilePersistResponse.builder()
			.status(status)
			.filePath(filePath)
			.build();
	}
}
