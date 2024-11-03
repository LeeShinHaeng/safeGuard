package com.capstone.safeGuard.apis.notice.presentation.request.emergency;

import lombok.Getter;

@Getter
public class CommentRequestDTO {
    private String commentatorId;
    private String commentContent;
    private String emergencyId;
}
