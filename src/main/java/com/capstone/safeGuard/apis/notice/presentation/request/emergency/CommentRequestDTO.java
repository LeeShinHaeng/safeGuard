package com.capstone.safeGuard.apis.notice.presentation.request.emergency;

public record CommentRequestDTO(
	String commentatorId,
	String commentContent,
	String emergencyId
) {
}
