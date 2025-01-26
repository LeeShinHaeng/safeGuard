package com.capstone.safeGuard.apis.comment.presentation.response;

import lombok.Builder;

@Builder
public record CommentResponse(
	String content,
	String commentator,
	String commentDate
) {
	public CommentResponse(String content, String commentator, String commentDate) {
		this.content = content;
		this.commentator = commentator;
		this.commentDate = commentDate;
	}
}
