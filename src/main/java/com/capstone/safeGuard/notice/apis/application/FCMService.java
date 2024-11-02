package com.capstone.safeGuard.notice.apis.application;

import com.capstone.safeGuard.member.domain.domain.Member;
import com.capstone.safeGuard.notice.apis.presentation.request.notification.FCMNotificationDTO;
import com.capstone.safeGuard.member.domain.infrastructure.MemberRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FCMService {
    private final FirebaseMessaging firebaseMessaging;
    private final MemberRepository memberRepository;

    public String SendNotificationByToken(FCMNotificationDTO dto) {
        log.info("Sending notification to FCM");
        Optional<Member> foundMember = memberRepository.findById(dto.getReceiverId());

        if (foundMember.isEmpty()) {
            return null;
        }

        if (foundMember.get().getFcmToken() == null) {
            log.info("FCM 토큰이 없음");
            return "";
        }

        Notification notification = Notification.builder()
                .setTitle(dto.title)
                .setBody(dto.body)
                .build();

        Message message = Message.builder()
                .setToken(foundMember.get().getFcmToken())
                .setNotification(notification)
                .build();

        try {
            firebaseMessaging.send(message);
            log.info("Sent notification 성공!!");
            return "성공";
        } catch (FirebaseMessagingException e) {
            log.info(e.getMessage());
            log.info("FIREBASE 문제");
            return null;
        }
    }
}
