package com.capstone.safeGuard.dto.request.emergency;

import jakarta.validation.constraints.AssertFalse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FcmMessageDTO {
    private boolean validateOnly;
    private FcmMessageDTO.Message message;


    @Builder
    @Getter @AllArgsConstructor
    public static class Message {
        private FcmMessageDTO.Notification notification;
        private String token;
    }

    @Builder
    @Getter @AllArgsConstructor
    public static class Notification {
        private String title;
        private String body;
    }

}
