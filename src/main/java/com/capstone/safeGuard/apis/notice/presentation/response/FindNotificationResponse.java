package com.capstone.safeGuard.apis.notice.presentation.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindNotificationResponse {
    private String title;
    private String content;
    private String date;
    private String child;
    private String senderId;

    @Builder
    public FindNotificationResponse(String title, String content, String date, String child, String senderId) {
        this.title = title;
        this.content = content;
        this.date = date;
        this.child = child;
        this.senderId = senderId;
    }
}
