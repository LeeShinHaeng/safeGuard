package com.capstone.safeGuard.apis.notice.presentation;

import com.capstone.safeGuard.apis.general.presentation.response.StatusOnlyResponse;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.GetIdRequest;
import com.capstone.safeGuard.apis.notice.application.NoticeService;
import com.capstone.safeGuard.apis.notice.presentation.request.fatal.FatalRequest;
import com.capstone.safeGuard.apis.notice.presentation.response.FindNotificationResponse;
import com.capstone.safeGuard.domain.member.domain.Child;
import com.capstone.safeGuard.domain.member.domain.Parenting;
import com.capstone.safeGuard.domain.member.infrastructure.ChildRepository;
import com.capstone.safeGuard.domain.notice.domain.Notice;
import com.capstone.safeGuard.domain.notice.domain.NoticeLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class NoticeController {
	private final NoticeService noticeService;
	private final ChildRepository childRepository;

	@PostMapping("/received-notice")
	public ResponseEntity<Map<String, FindNotificationResponse>> receivedNotice(@RequestBody GetIdRequest dto) {
		HashMap<String, FindNotificationResponse> result = new HashMap<>();

		List<Notice> noticeList = noticeService.findNoticeByMember(dto.id());

		for (Notice notice : noticeList) {
			String tmpId;
			if (notice.getNoticeLevel().equals(NoticeLevel.WARN)) {
				tmpId = "위험구역";
			} else if (notice.getNoticeLevel().equals(NoticeLevel.INFO)) {
				tmpId = "구역이동";
			} else {
				tmpId = "위험신호알림";
			}
			String format = notice.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

			result.put(notice.getNoticeId() + "",
				FindNotificationResponse.of(
					tmpId,
					notice.getContent(),
					format,
					notice.getChild().getChildName()
				)
			);
		}

		return ResponseEntity.ok().body(result);
	}

	@PostMapping("/fatal")
	public ResponseEntity<StatusOnlyResponse> fatal(@RequestBody FatalRequest dto) {
		Child foundChild = childRepository.findByChildName(dto.childName())
			.orElseThrow(() -> new RuntimeException("Child not found"));

		List<Parenting> childParentingList = foundChild.getParentingList();
		noticeService.sendNoticeToMember(childParentingList, foundChild.getChildName(), NoticeLevel.FATAL);

		return ResponseEntity.ok(new StatusOnlyResponse(200));
	}
}

