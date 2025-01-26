package com.capstone.safeGuard.apis.comment.presentation.response;

import lombok.Builder;

import java.util.Map;

@Builder
public record CommentListResponse(
	Map<String, CommentResponse> data
) {
	public static CommentListResponse from(Map<String, CommentResponse> data) {
		return CommentListResponse.builder()
			.data(data)
			.build();
	}
}
