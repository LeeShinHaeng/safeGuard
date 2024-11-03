package com.capstone.safeGuard.apis.comment.presentation.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CommentResponseDTO {
    private String content;
    private String commentator;
    private String commentDate;

    @Builder

    public CommentResponseDTO(String content, String commentator, String commentDate) {
        this.content = content;
        this.commentator = commentator;
        this.commentDate = commentDate;
    }
}
