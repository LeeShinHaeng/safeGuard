package com.capstone.safeGuard.apis.notice.application;

import com.capstone.safeGuard.apis.notice.presentation.request.notification.FCMNotificationDTO;
import com.capstone.safeGuard.domain.member.domain.Member;
import com.capstone.safeGuard.domain.member.infrastructure.MemberRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FCMService {
	private final FirebaseMessaging firebaseMessaging;
	private final MemberRepository memberRepository;

	public void SendNotificationByToken(FCMNotificationDTO dto) {
		log.info("Sending notification to FCM");
		Member foundMember = memberRepository.findById(dto.receiverId())
			.orElseThrow(() -> new RuntimeException("Member not found!"));

		if (foundMember.getFcmToken() == null) {
			throw new RuntimeException("FCM Token not found");
		}

		Notification notification = Notification.builder()
			.setTitle(dto.title())
			.setBody(dto.body())
			.build();

		Message message = Message.builder()
			.setToken(foundMember.getFcmToken())
			.setNotification(notification)
			.build();

		try {
			firebaseMessaging.send(message);
		} catch (FirebaseMessagingException e) {
			throw new RuntimeException("Failed to send notification because of FCM Internal Error", e);
		}
	}
}
