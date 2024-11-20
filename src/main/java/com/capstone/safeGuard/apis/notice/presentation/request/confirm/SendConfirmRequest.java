package com.capstone.safeGuard.apis.notice.presentation.request.confirm;

public record SendConfirmRequest(
	String senderId,
	String childName,
	String confirmType
) {
}
