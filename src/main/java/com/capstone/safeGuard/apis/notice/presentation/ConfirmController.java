package com.capstone.safeGuard.apis.notice.presentation;

import com.capstone.safeGuard.apis.member.application.MemberService;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.GetIdDTO;
import com.capstone.safeGuard.apis.notice.application.ConfirmService;
import com.capstone.safeGuard.apis.notice.presentation.request.confirm.SendConfirmRequest;
import com.capstone.safeGuard.apis.notice.presentation.response.FindNotificationResponse;
import com.capstone.safeGuard.domain.member.domain.Child;
import com.capstone.safeGuard.domain.member.domain.Helping;
import com.capstone.safeGuard.domain.member.domain.Member;
import com.capstone.safeGuard.domain.member.infrastructure.MemberRepository;
import com.capstone.safeGuard.domain.notice.domain.Confirm;
import com.capstone.safeGuard.domain.notice.domain.ConfirmType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ConfirmController {
	private final MemberService memberService;
	private final MemberRepository memberRepository;
	private final ConfirmService confirmService;

	@Transactional
	@PostMapping("/send-confirm")
	public ResponseEntity<Map<String, String>> sendConfirm(@RequestBody SendConfirmRequest dto) {
		Map<String, String> result = new HashMap<>();

		// 1. chidname 확인
		Child foundChild = memberService.findChildByChildName(dto.getChildName());
		if (foundChild == null) {
			return addErrorStatus(result);
		}

		Optional<Member> foundSender = memberRepository.findById(dto.getSenderId());
		if (foundSender.isEmpty()) {
			return addErrorStatus(result);
		}

		// 2. 해당 child의 member에게 전송
		ArrayList<Member> foundMemberList = memberService.findAllParentByChild(foundChild);
		if (foundMemberList == null) {
			return addErrorStatus(result);
		}

		// helper가 helpinglist에 존재하는지 확인
		List<Helping> childHelpingList = foundChild.getHelpingList();
		if (childHelpingList == null) {
			return addErrorStatus(result);
		}

		boolean isSent = false;
		for (Helping helping : childHelpingList) {
			if (helping.getHelper().equals(foundSender.get())) {
				// helper가 존재하면 confirm 전송
				isSent = sendConfirmToAllMember(foundMemberList, foundChild, helping, dto.getConfirmType());
			}
		}
		if (!isSent) {
			return addErrorStatus(result);
		}

		return addOkStatus(result);
	}

	@Transactional
	public boolean sendConfirmToAllMember(ArrayList<Member> foundMemberList, Child foundChild, Helping helping, String confirmType) {
		for (Member member : foundMemberList) {
			if (!sendConfirmToMember(member, foundChild, helping, confirmType)) {
				return false;
			}
		}
		return true;
	}

	@Transactional
	public boolean sendConfirmToMember(Member receiverId, Child child, Helping helping, String confirmType) {
		Confirm confirm = confirmService.saveConfirm(receiverId, child, helping, confirmType);
		log.info("Confirm save to member " + receiverId.getMemberId());
		if (confirm == null) {
			return false;
		}

		return confirmService.sendNotificationTo(receiverId.getMemberId(), confirm);
	}

	@PostMapping("/received-confirm")
	public ResponseEntity<Map<String, FindNotificationResponse>> receivedConfirm(@RequestBody GetIdDTO dto) {
		HashMap<String, FindNotificationResponse> result = new HashMap<>();

		List<Confirm> confirmList = confirmService.findReceivedConfirmByMember(dto.getId());
		if (confirmList == null || confirmList.isEmpty()) {
			return ResponseEntity.status(400).body(result);
		}
		for (Confirm confirm : confirmList) {
			String tmpId;
			if (confirm.getConfirmType().equals(ConfirmType.ARRIVED)) {
				tmpId = "도착";
			} else if (confirm.getConfirmType().equals(ConfirmType.DEPART)) {
				tmpId = "출발";
			} else {
				tmpId = "미확인";
			}

			String format = confirm.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

			result.put(confirm.getConfirmId() + "",
				FindNotificationResponse.builder()
					.child(confirm.getChild().getChildName())
					.title(tmpId)
					.content(confirm.getContent())
					.date(format)
					.senderId(confirm.getHelpingId().getHelper().getMemberId())
					.build()
			);
		}

		return ResponseEntity.ok().body(result);
	}

	@PostMapping("/sent-confirm")
	public ResponseEntity<Map<String, FindNotificationResponse>> sentConfirm(@RequestBody GetIdDTO dto) {
		HashMap<String, FindNotificationResponse> result = new HashMap<>();

		List<Confirm> confirmList = confirmService.findSentConfirmByMember(dto.getId());
		if (confirmList == null || confirmList.isEmpty()) {
			return ResponseEntity.status(400).body(result);
		}
		for (Confirm confirm : confirmList) {
			String tmpId;
			if (confirm.getConfirmType().equals(ConfirmType.ARRIVED)) {
				tmpId = "도착";
			} else if (confirm.getConfirmType().equals(ConfirmType.DEPART)) {
				tmpId = "출발";
			} else {
				tmpId = "미확인";
			}

			String format = confirm.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

			result.put(confirm.getConfirmType() + "",
				FindNotificationResponse.builder()
					.child(confirm.getChild().getChildName())
					.title(tmpId)
					.content(confirm.getContent())
					.date(format)
					.senderId(confirm.getHelpingId().getHelper().getMemberId())
					.build()
			);
		}

		return ResponseEntity.ok().body(result);
	}

	private static ResponseEntity<Map<String, String>> addOkStatus(Map<String, String> result) {
		result.put("status", "200");
		return ResponseEntity.ok().body(result);
	}

	private static ResponseEntity<Map<String, String>> addErrorStatus(Map<String, String> result) {
		result.put("status", "400");
		return ResponseEntity.status(400).body(result);
	}
}
