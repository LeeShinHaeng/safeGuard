package com.capstone.safeGuard.apis.notice.presentation.response;

import lombok.Builder;

@Builder
public record FindNotificationResponse(
	String title,
	String content,
	String date,
	String child,
	String senderId
) {
	public static FindNotificationResponse of(String title, String content, String date, String child, String senderId) {
		return FindNotificationResponse.builder()
			.title(title)
			.content(content)
			.date(date)
			.child(child)
			.senderId(senderId)
			.build();
	}

	public static FindNotificationResponse of(String title, String content, String date, String child) {
		return FindNotificationResponse.builder()
			.title(title)
			.content(content)
			.date(date)
			.child(child)
			.senderId(null)
			.build();
	}
}
