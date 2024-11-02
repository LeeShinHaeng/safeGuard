package com.capstone.safeGuard.notice.apis.presentation.request.emergency;

import lombok.Getter;

@Getter
public class CommentRequestDTO {
    private String commentatorId;
    private String commentContent;
    private String emergencyId;
}
