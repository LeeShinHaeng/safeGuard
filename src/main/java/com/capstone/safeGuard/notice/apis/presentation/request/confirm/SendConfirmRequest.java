package com.capstone.safeGuard.notice.apis.presentation.request.confirm;

import lombok.Getter;

@Getter
public class SendConfirmRequest{
    private String senderId;
    private String childName;
    private String confirmType;
}
