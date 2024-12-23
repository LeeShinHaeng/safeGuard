package com.capstone.safeGuard.apis.notice.application;

import com.capstone.safeGuard.apis.member.application.MemberUtil;
import com.capstone.safeGuard.apis.notice.presentation.request.notification.FCMNotificationDTO;
import com.capstone.safeGuard.domain.map.domain.Coordinate;
import com.capstone.safeGuard.domain.map.infrastructure.CoordinateRepository;
import com.capstone.safeGuard.domain.member.domain.Child;
import com.capstone.safeGuard.domain.member.domain.Member;
import com.capstone.safeGuard.domain.member.domain.Parenting;
import com.capstone.safeGuard.domain.notice.domain.Notice;
import com.capstone.safeGuard.domain.notice.domain.NoticeLevel;
import com.capstone.safeGuard.domain.notice.infrastructure.NoticeRepository;
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
public class NoticeService {
	private final NoticeRepository noticeRepository;
	private final FCMService fcmService;
	private final CoordinateRepository coordinateRepository;
	private final MemberUtil memberUtil;

	@Transactional
	public Notice createNotice(String receiverId, String childName, NoticeLevel noticeLevel) {
		Member member = memberUtil.findMemberById(receiverId);

		Notice notice = new Notice();
		notice.setTitle(noticeLevel.name());
		notice.setContent("피보호자 이름 : " + childName);
		notice.setReceiverId(member.getMemberId());
		notice.setNoticeLevel(noticeLevel);
		notice.setChild(memberUtil.findChildByName(childName));
		notice.setCreatedAt(LocalDateTime.now());

		return noticeRepository.save(notice);
	}

	@Transactional
	public void sendNotice(String childName) {
		Child foundChild = memberUtil.findChildByName(childName);

		String currentStatus = getCurrentStatus(foundChild);
		String lastStatus = "일반구역";
		if (foundChild.getLastStatus() != null) {
			lastStatus = foundChild.getLastStatus();
		} else {
			foundChild.setLastStatus(lastStatus);
		}
		log.warn("{}의 currentStatus : {} | lastStatus : {} ", childName, currentStatus, foundChild.getLastStatus());


		List<Parenting> childParentingList = foundChild.getParentingList();
		// 구역 변경 시 FCM 메시지 전송
		if (currentStatus.equals("위험구역") && (!lastStatus.equals("위험구역"))) {
			sendNoticeToMember(childParentingList, foundChild.getChildName(), NoticeLevel.WARN);
			// 마지막 상태 갱신
			foundChild.setLastStatus(currentStatus);
			log.warn("warn 전송 완료");
		} //else if ( lastStatus.equals("위험구역") && (currentStatus.equals("일반구역") || currentStatus.equals("안전구역")) ) {
		else if (!lastStatus.equals(currentStatus)) {
			sendNoticeToMember(childParentingList, foundChild.getChildName(), NoticeLevel.INFO);
			// 마지막 상태 갱신
			foundChild.setLastStatus(currentStatus);
			log.warn("info 전송 완료");
		}
	}

	@Transactional
	public String getCurrentStatus(Child foundChild) {
		double[] childPosition = {foundChild.getLatitude(), foundChild.getLongitude()};

		ArrayList<Coordinate> coordinateArrayList = coordinateRepository.findAllByChild(foundChild);
		for (Coordinate coordinate : coordinateArrayList) {
			double[][] polygon = {
				{coordinate.getYOfNorthWest(), coordinate.getXOfNorthWest()},
				{coordinate.getYOfNorthEast(), coordinate.getXOfNorthEast()},
				{coordinate.getYOfSouthEast(), coordinate.getXOfSouthEast()},
				{coordinate.getYOfSouthWest(), coordinate.getXOfSouthWest()}
			};

			if (coordinate.isLivingArea()) {
				if (isPointInPolygon(polygon, childPosition)) {
					return "안전구역";
				}
			} else {
				if (isPointInPolygon(polygon, childPosition)) {
					return "위험구역";
				}
			}
		}

		return "일반구역";
	}

	@Transactional
	public void sendNoticeToMember(List<Parenting> parentingList, String childName, NoticeLevel noticeLevel) {
		for (Parenting parenting : parentingList) {
			Notice notice = createNotice(
				parenting.getParent().getMemberId(),
				childName,
				noticeLevel
			);
			sendNotificationTo(notice);
		}
	}

	public static boolean isPointInPolygon(double[][] polygon, double[] point) {
		int n = polygon.length;
		double px = point[0], py = point[1];
		boolean inside = false;

		for (int i = 0, j = n - 1; i < n; j = i++) {
			double ix = polygon[i][0], iy = polygon[i][1];
			double jx = polygon[j][0], jy = polygon[j][1];

			if ((iy > py) != (jy > py) &&
				(px < (jx - ix) * (py - iy) / (jy - iy) + ix)) {
				inside = !inside;
			}
		}

		return inside;
	}

	public void sendNotificationTo(Notice notice) {
		FCMNotificationDTO message = makeMessage(notice);
		fcmService.SendNotificationByToken(message);
	}

	private FCMNotificationDTO makeMessage(Notice notice) {
		if (notice == null) {
			return null;
		}

		String tmpTitle = "피보호자가 위험 신호를 보냈습니다!";
		if (notice.getNoticeLevel().equals(NoticeLevel.INFO)) {
			tmpTitle = "피보호자의 구역이 변경되었습니다.";
		} else if (notice.getNoticeLevel().equals(NoticeLevel.WARN)) {
			tmpTitle = "피보호자가 위험 구역에 진입하였습니다.";
		}

		return FCMNotificationDTO.of(
			notice.getReceiverId(), tmpTitle, notice.getContent()
		);
	}

	public List<Notice> findNoticeByMember(String memberId) {
		memberUtil.findMemberById(memberId);

		return noticeRepository.findAllByReceiverId(memberId);
	}
}
