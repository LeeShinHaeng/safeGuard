package com.capstone.safeGuard.apis.notice.presentation.request.confirm;

import lombok.Getter;

@Getter
public class SendConfirmRequest{
    private String senderId;
    private String childName;
    private String confirmType;
}
