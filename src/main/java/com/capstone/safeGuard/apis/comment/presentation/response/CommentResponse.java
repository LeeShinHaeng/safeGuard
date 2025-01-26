package com.capstone.safeGuard.apis.comment.presentation.response;

import lombok.Builder;

@Builder
public record CommentResponse(
	String content,
	String commentator,
	String commentDate
) {
	public static CommentResponse of(String content, String commentator, String commentDate) {
		return CommentResponse.builder()
			.content(content)
			.commentator(commentator)
			.commentDate(commentDate)
			.build();
	}
}
