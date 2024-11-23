package com.capstone.safeGuard.apis.notice.application;

import com.capstone.safeGuard.apis.member.application.MemberService;
import com.capstone.safeGuard.apis.notice.presentation.request.notification.FCMNotificationDTO;
import com.capstone.safeGuard.domain.member.domain.Child;
import com.capstone.safeGuard.domain.member.domain.Helping;
import com.capstone.safeGuard.domain.member.domain.Member;
import com.capstone.safeGuard.domain.member.domain.Parenting;
import com.capstone.safeGuard.domain.member.infrastructure.HelpingRepository;
import com.capstone.safeGuard.domain.notice.domain.Confirm;
import com.capstone.safeGuard.domain.notice.domain.ConfirmType;
import com.capstone.safeGuard.domain.notice.infrastructure.ConfirmRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfirmService {
	private final ConfirmRepository confirmRepository;
	private final FCMService fcmService;
	private final MemberService memberService;
	private final HelpingRepository helpingRepository;

	@Transactional
	public void sendConfirmToAllMember(ArrayList<Member> foundMemberList, Child foundChild, Helping helping, String confirmType) {
		for (Member member : foundMemberList) {
			sendConfirmToMember(member, foundChild, helping, confirmType);
		}
	}

	@Transactional
	public void sendConfirmToMember(Member receiverId, Child child, Helping helping, String confirmType) {
		Confirm confirm = saveConfirm(receiverId, child, helping, confirmType);
		if (confirm == null) {
			throw new RuntimeException("Save Confirm Failed");
		}

		sendNotificationTo(receiverId.getMemberId(), confirm);
	}

	@Transactional
	public Confirm saveConfirm(Member receiverId, Child child, Helping helping, String confirmType) {
		Confirm confirm = new Confirm();
		if (confirmType.equals("ARRIVED")) {
			confirm.setConfirmType(ConfirmType.ARRIVED);
		} else if (confirmType.equals("DEPART")) {
			confirm.setConfirmType(ConfirmType.DEPART);
		} else {
			confirm.setConfirmType(ConfirmType.UNCONFIRMED);
		}

		confirm.setChild(child);
		confirm.setTitle(confirmType);
		confirm.setContent("피보호자 이름 : " + child.getChildName());
		confirm.setCreatedAt(LocalDateTime.now());
		confirm.setHelpingId(helping);
		confirm.setReceiverId(receiverId);
		log.info("Confirm save : " + confirm.getConfirmId());
		confirmRepository.save(confirm);

		return confirm;
	}

	public void sendNotificationTo(String receiverId, Confirm confirm) {
		log.info("Confirm send : " + confirm.getConfirmId());
		FCMNotificationDTO message = makeMessage(receiverId, confirm);
		fcmService.SendNotificationByToken(message);
	}

	private FCMNotificationDTO makeMessage(String receiverId, Confirm confirm) {

		String tmpTitle = "피보호자의 도착이 확인되지 않습니다.";
		if (confirm.getConfirmType().equals(ConfirmType.ARRIVED)) {
			tmpTitle = "피보호자가 도착 완료했습니다.";
		} else if (confirm.getConfirmType().equals(ConfirmType.DEPART)) {
			tmpTitle = "피보호자가 출발했습니다.";
		}

		return FCMNotificationDTO.of(
			receiverId, tmpTitle, confirm.getContent()
		);
	}

	@Transactional
	public List<Confirm> findReceivedConfirmByMember(String id) {
		Member foundMember = getMember(id);

		List<Parenting> parentingList = foundMember.getParentingList();
		if (parentingList == null || parentingList.isEmpty()) {
			log.info("No parenting found for id : " + id);
			return null;
		}

		ArrayList<Confirm> confirmList = new ArrayList<>();
		for (Parenting parenting : parentingList) {
			confirmList.addAll(confirmRepository
				.findAllByReceiverId(parenting.getParent()));
		}

		return confirmList;
	}

	public ArrayList<Confirm> findSentConfirmByMember(String id) {
		Member foundMember = getMember(id);

		List<Helping> helpingList = helpingRepository.findAllByHelper(foundMember);
		if (helpingList.isEmpty()) {
			log.info("No helping found for id : " + id);
			return null;
		}

		ArrayList<Confirm> confirmList = new ArrayList<>();
		for (Helping helping : helpingList) {
			confirmList.addAll(confirmRepository.findAllByHelpingId(helping));
		}

		return confirmList;
	}

	private Member getMember(String id) {
		return memberService.findMemberById(id);
	}
}