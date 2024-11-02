package com.capstone.safeGuard.apis.notice.application;

import com.capstone.safeGuard.domain.member.domain.Child;
import com.capstone.safeGuard.domain.notice.domain.Notice;
import com.capstone.safeGuard.domain.notice.domain.NoticeLevel;
import com.capstone.safeGuard.apis.notice.presentation.request.notification.FCMNotificationDTO;
import com.capstone.safeGuard.domain.member.infrastructure.ChildRepository;
import com.capstone.safeGuard.domain.member.infrastructure.MemberRepository;
import com.capstone.safeGuard.domain.notice.infrastructure.NoticeRepository;
import com.capstone.safeGuard.apis.member.application.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoticeService {
    private final NoticeRepository noticeRepository;
    private final MemberRepository memberRepository;
    private final ChildRepository childRepository;
    private final FCMService fcmService;
    private final MemberService memberService;

    @Transactional
    public Notice createNotice(String receiverId, String childName, NoticeLevel noticeLevel) {
        Notice notice = new Notice();
        notice.setTitle(noticeLevel.name());
        notice.setContent("피보호자 이름 : " + childName);
        notice.setReceiverId(receiverId);

        if(! memberRepository.existsByMemberId(receiverId)) {
            log.info("createNotice memberId not exist!!");
            return null;
        }
        notice.setNoticeLevel(noticeLevel);

        Child child = childRepository.findByChildName(childName);

        if (child == null) {
            log.info("createNotice childName not exist!!");
            return null;
        }
        notice.setChild(child);
        notice.setCreatedAt(LocalDateTime.now());

        noticeRepository.save(notice);

        return notice;
    }

    public boolean sendNotificationTo(Notice notice){
        FCMNotificationDTO message = makeMessage(notice);
        return fcmService.SendNotificationByToken(message) != null;
    }

    private FCMNotificationDTO makeMessage(Notice notice) {
        if(notice == null) {
            return null;
        }

        String tmpTitle = "피보호자가 위험 신호를 보냈습니다!";
        if(notice.getNoticeLevel().equals(NoticeLevel.INFO)) {
            tmpTitle = "피보호자의 구역이 변경되었습니다.";
        } else if (notice.getNoticeLevel().equals(NoticeLevel.WARN)){
            tmpTitle = "피보호자가 위험 구역에 진입하였습니다.";
        }

        return FCMNotificationDTO.builder()
                .title(tmpTitle)
                .body(notice.getContent())
                .receiverId(notice.getReceiverId())
                .build();
    }

    public List<Notice> findNoticeByMember(String memberId) {
        if(memberService.findMemberById(memberId) == null) {
            log.info("No such member!!");
            return null;
        }

        List<Notice> foundNoticeList = noticeRepository.findAllByReceiverId(memberId);
        if(foundNoticeList.isEmpty()){
            log.info("Notice doesn't exist!!");
            return null;
        }
        return foundNoticeList;
    }
}
