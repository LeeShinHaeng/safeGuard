package com.capstone.safeGuard.apis.notice.presentation;

import com.capstone.safeGuard.apis.general.presentation.response.StatusOnlyResponse;
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

	@PostMapping("/send-confirm")
	public ResponseEntity<StatusOnlyResponse> sendConfirm(@RequestBody SendConfirmRequest dto) {
		// 1. chidname 확인
		Child foundChild = memberService.findChildByChildName(dto.childName());
		if (foundChild == null) {
			return addErrorStatus();
		}

		Optional<Member> foundSender = memberRepository.findById(dto.senderId());
		if (foundSender.isEmpty()) {
			return addErrorStatus();
		}

		// 2. 해당 child의 member에게 전송
		ArrayList<Member> foundMemberList = memberService.findAllParentByChild(foundChild);
		if (foundMemberList == null) {
			return addErrorStatus();
		}

		// helper가 helpinglist에 존재하는지 확인
		List<Helping> childHelpingList = foundChild.getHelpingList();
		if (childHelpingList == null) {
			return addErrorStatus();
		}

		boolean isSent = false;
		for (Helping helping : childHelpingList) {
			if (helping.getHelper().equals(foundSender.get())) {
				// helper가 존재하면 confirm 전송
				isSent = confirmService.sendConfirmToAllMember(foundMemberList, foundChild, helping, dto.confirmType());
			}
		}
		if (!isSent) {
			return addErrorStatus();
		}

		return addOkStatus();
	}

	@PostMapping("/received-confirm")
	public ResponseEntity<Map<String, FindNotificationResponse>> receivedConfirm(@RequestBody GetIdDTO dto) {
		HashMap<String, FindNotificationResponse> result = new HashMap<>();

		List<Confirm> confirmList = confirmService.findReceivedConfirmByMember(dto.id());
		if (confirmList == null || confirmList.isEmpty()) {
			return ResponseEntity.status(400).body(result);
		}
		for (Confirm confirm : confirmList) {
			String tmpId = extractTmpId(confirm.getConfirmType());
			String format = confirm.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

			result.put(confirm.getConfirmId() + "",
				FindNotificationResponse.of(
					tmpId,
					confirm.getContent(),
					format,
					confirm.getChild().getChildName(),
					confirm.getHelpingId().getHelper().getMemberId()
				)
			);
		}

		return ResponseEntity.ok().body(result);
	}

	@PostMapping("/sent-confirm")
	public ResponseEntity<Map<String, FindNotificationResponse>> sentConfirm(@RequestBody GetIdDTO dto) {
		HashMap<String, FindNotificationResponse> result = new HashMap<>();

		List<Confirm> confirmList = confirmService.findSentConfirmByMember(dto.id());
		if (confirmList == null || confirmList.isEmpty()) {
			return ResponseEntity.status(400).body(result);
		}
		for (Confirm confirm : confirmList) {
			String tmpId = extractTmpId(confirm.getConfirmType());
			String format = confirm.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

			result.put(confirm.getConfirmType() + "",
				FindNotificationResponse.of(
					tmpId,
					confirm.getContent(),
					format,
					confirm.getChild().getChildName(),
					confirm.getHelpingId().getHelper().getMemberId()
				)
			);
		}

		return ResponseEntity.ok().body(result);
	}

	private String extractTmpId(ConfirmType confirm) {
		return switch (confirm) {
			case ARRIVED -> "도착";
			case DEPART -> "출발";
			case UNCONFIRMED -> "미확인";
		};
	}

	private static ResponseEntity<StatusOnlyResponse> addOkStatus() {
		return ResponseEntity.ok(StatusOnlyResponse.of(200));
	}


	private static ResponseEntity<StatusOnlyResponse> addErrorStatus() {
		return ResponseEntity.status(400)
			.body(StatusOnlyResponse.of(400));
	}
}
