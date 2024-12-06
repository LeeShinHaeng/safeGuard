package com.capstone.safeGuard.apis.notice.presentation.request.emergency;

import com.capstone.safeGuard.domain.member.domain.Child;
import com.capstone.safeGuard.domain.member.domain.Member;
import com.capstone.safeGuard.domain.notice.domain.Emergency;

import java.time.LocalDateTime;

public record EmergencyRequestDTO (
	String senderId,
	String childName,
	double latitude,
	double longitude,
	String title
) {
	public Emergency dtoToDomain(Member member, Child child, String content) {
		return Emergency
			.builder()
			.title(title)
			.content(content)
			.senderId(member)
			.child(child)
			.title("주변 피보호자에게 도움이 필요합니다.")
			.createdAt(LocalDateTime.now())
			.build();
	}
}
