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

	public boolean SendNotificationByToken(FCMNotificationDTO dto) {
		log.info("Sending notification to FCM");
		Member foundMember = memberRepository.findById(dto.receiverId())
			.orElseThrow(() -> new RuntimeException("Member not found!"));

		if (foundMember.getFcmToken() == null) {
			log.info("FCM 토큰이 없음. 해당 멤버를 건너뜀");
			return false;
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
			log.info("Sent notification 성공!!");
			return true;
		} catch (FirebaseMessagingException e) {
			log.info(e.getMessage());
			log.info("FIREBASE 문제");
			return false;
		}
	}
}
