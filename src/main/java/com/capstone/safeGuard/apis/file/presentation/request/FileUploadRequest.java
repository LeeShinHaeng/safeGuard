package com.capstone.safeGuard.apis.file.presentation.request;

public record FileUploadRequest(
	String uploaderType,
	String uploaderId
) {
}
