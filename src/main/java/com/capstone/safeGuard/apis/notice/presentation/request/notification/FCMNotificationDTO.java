package com.capstone.safeGuard.apis.notice.presentation.request.notification;

import lombok.Builder;

@Builder
public record FCMNotificationDTO(
	String receiverId,
	String title,
	String body
) {
	public static FCMNotificationDTO of(String receiverId, String title, String body) {
		return FCMNotificationDTO.builder()
			.receiverId(receiverId)
			.title(title)
			.body(body)
			.build();
	}
}
