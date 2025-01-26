package com.capstone.safeGuard.apis.notice.presentation.response;

import lombok.Builder;

import java.util.Map;

@Builder
public record NotificationListResponse(
	Map<String, FindNotificationResponse> data
) {
	public static NotificationListResponse from(Map<String, FindNotificationResponse> data) {
		return NotificationListResponse.builder()
			.data(data)
			.build();
	}
}
