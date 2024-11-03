package com.capstone.safeGuard.apis.notice.presentation.request.notification;

import lombok.Builder;
import lombok.Getter;

@Getter
public class FCMNotificationDTO {
    public String receiverId;
    public String title;
    public String body;

    @Builder
    public FCMNotificationDTO(String receiverId, String title, String body) {
        this.receiverId = receiverId;
        this.title = title;
        this.body = body;
    }
}
