package com.capstone.safeGuard.apis.notice.presentation;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import com.capstone.safeGuard.apis.comment.presentation.response.CommentResponseDTO;
import com.capstone.safeGuard.apis.general.presentation.response.StatusOnlyResponse;
import com.capstone.safeGuard.apis.notice.application.EmergencyService;
import com.capstone.safeGuard.apis.notice.presentation.request.emergency.CommentIdDTO;
import com.capstone.safeGuard.apis.notice.presentation.request.emergency.CommentRequestDTO;
import com.capstone.safeGuard.apis.notice.presentation.request.emergency.EmergencyIdDTO;
import com.capstone.safeGuard.apis.notice.presentation.request.emergency.EmergencyRequestDTO;
import com.capstone.safeGuard.apis.notice.presentation.request.emergency.MemberIdDTO;
import com.capstone.safeGuard.apis.notice.presentation.response.FindNotificationResponse;
import com.capstone.safeGuard.domain.comment.domain.Comment;
import com.capstone.safeGuard.domain.notice.domain.Emergency;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class EmergencyController {
	// 몇 km 이내의 사람들에게 알림을 보낼 것인지
	static public final int DISTANCE = 1;
	private final EmergencyService emergencyService;

	@PostMapping("/emergency")
	public ResponseEntity<StatusOnlyResponse> emergencyCall(@RequestBody EmergencyRequestDTO emergencyRequestDto) {
		// 1. 반경 [] km내의 member들만 리스트업
		ArrayList<String> neighborMemberList
			= emergencyService.getNeighborMembers(emergencyRequestDto, DISTANCE);

		if (neighborMemberList.isEmpty()) {
			return addOkStatus();
		}

		// 2. 반경 []km 내의 member 들에게 알림을 보냄
		if (!emergencyService.sendEmergencyToMembers(neighborMemberList, emergencyRequestDto)) {
			return addErrorStatus();
		}

		return addOkStatus();
	}

	private static ResponseEntity<StatusOnlyResponse> addOkStatus() {
		return ResponseEntity.ok(StatusOnlyResponse.of(200));
	}

	private static ResponseEntity<StatusOnlyResponse> addErrorStatus() {
		return ResponseEntity.status(BAD_REQUEST)
			.body(StatusOnlyResponse.of(400));
	}

	@PostMapping("/sent-emergency")
	public ResponseEntity<Map<String, FindNotificationResponse>> showSentEmergency(@RequestBody MemberIdDTO dto) {
		List<Emergency> sentEmergencyList = emergencyService.getSentEmergency(dto.getMemberId());

		HashMap<String, FindNotificationResponse> result = addEmergencyList(sentEmergencyList);
		if (result == null) {
			return ResponseEntity.status(400).body(null);
		}

		return ResponseEntity.ok().body(result);
	}

	@PostMapping("/received-emergency")
	public ResponseEntity<Map<String, FindNotificationResponse>> showReceivedEmergency(@RequestBody MemberIdDTO dto) {
		List<Emergency> receivedEmergencyList = emergencyService.getReceivedEmergency(dto.getMemberId());

		HashMap<String, FindNotificationResponse> result = addEmergencyList(receivedEmergencyList);
		if (result == null) {
			return ResponseEntity.status(400).body(null);
		}

		return ResponseEntity.ok().body(result);
	}

	@PostMapping("/write-comment")
	public ResponseEntity<StatusOnlyResponse> writeComment(@RequestBody CommentRequestDTO commentRequestDTO) {
		if (!emergencyService.writeComment(commentRequestDTO)) {
			return addErrorStatus();
		}

		return addOkStatus();
	}

	@PostMapping("/delete-comment")
	public ResponseEntity<StatusOnlyResponse> deleteComment(@RequestBody CommentIdDTO dto) {
		if (!emergencyService.deleteComment(dto.getCommentId())) {
			return addErrorStatus();
		}

		return addOkStatus();
	}

	@PostMapping("/emergency-detail")
	public ResponseEntity<Map<String, CommentResponseDTO>> emergencyDetail(@RequestBody EmergencyIdDTO dto) {
		HashMap<String, CommentResponseDTO> result = new HashMap<>();

		Emergency emergency = emergencyService.getEmergencyDetail(dto.getEmergencyId());
		if (emergency == null) {
			return ResponseEntity.status(400).body(null);
		}

		List<Comment> commentList = emergencyService.getCommentOfEmergency(dto.getEmergencyId());
		for (Comment comment : commentList) {
			String format = comment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

			result.put(comment.getCommentId().toString(),
				CommentResponseDTO.builder()
					.commentator(comment.getCommentator().getMemberId())
					.commentDate(format)
					.content(comment.getComment())
					.build()
			);
		}

		return ResponseEntity.ok().body(result);
	}

	private static HashMap<String, FindNotificationResponse> addEmergencyList(List<Emergency> sentEmergencyList) {
		HashMap<String, FindNotificationResponse> result = new HashMap<>();

		if (sentEmergencyList == null) {
			return null;
		}

		for (Emergency emergency : sentEmergencyList) {
			String format = emergency.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

			result.put(emergency.getEmergencyId() + "",
				FindNotificationResponse.of(
					"도움 요청",
					emergency.getContent(),
					format,
					emergency.getChild().getChildName(),
					emergency.getSenderId().getMemberId()
				)
			);
		}

		return result;
	}
}
