package com.capstone.safeGuard.apis.notice.application;

import com.capstone.safeGuard.apis.member.application.MemberService;
import com.capstone.safeGuard.apis.notice.presentation.request.emergency.CommentRequestDTO;
import com.capstone.safeGuard.apis.notice.presentation.request.emergency.EmergencyRequestDTO;
import com.capstone.safeGuard.apis.notice.presentation.request.notification.FCMNotificationDTO;
import com.capstone.safeGuard.apis.notice.presentation.response.FindNotificationResponse;
import com.capstone.safeGuard.domain.comment.domain.Comment;
import com.capstone.safeGuard.domain.comment.infrastructure.CommentRepository;
import com.capstone.safeGuard.domain.member.domain.Child;
import com.capstone.safeGuard.domain.member.domain.Member;
import com.capstone.safeGuard.domain.notice.domain.Emergency;
import com.capstone.safeGuard.domain.notice.domain.EmergencyReceiver;
import com.capstone.safeGuard.domain.notice.infrastructure.EmergencyReceiverRepository;
import com.capstone.safeGuard.domain.notice.infrastructure.EmergencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmergencyService {
	private final EmergencyRepository emergencyRepository;
	private final MemberService memberService;
	private final CommentRepository commentRepository;
	private final EmergencyReceiverRepository emergencyReceiverRepository;
	private final FCMService fcmService;


	@Transactional
	public void sendEmergencyToMembers(ArrayList<String> neighborMemberList, EmergencyRequestDTO dto) {
		for (String memberId : neighborMemberList) {
			// 3. 알림을 전송 및 저장
			Emergency emergency = saveEmergency(memberId, dto);
			if (!sendNotificationTo(memberId, emergency)) {
				throw new RuntimeException("Send Notification Failed");
			}
		}
	}

	public ArrayList<String> getNeighborMembers(EmergencyRequestDTO dto, int distance) {
		ArrayList<String> memberIdList = new ArrayList<>();
		ArrayList<Member> allMember = memberService.findAllMember();
		Child foundChild = memberService.findChildByChildName(dto.childName());

		for (Member member : allMember) {
			if (isNeighbor(foundChild.getLatitude(), foundChild.getLongitude(), member.getLatitude(), member.getLongitude(), distance)) {
				memberIdList.add(member.getMemberId());
			}
		}

		return memberIdList;
	}

	private boolean isNeighbor(double latitude, double longitude, double memberLatitude, double memberLongitude, int length) {
		double latitudeDistance = latitude - memberLatitude;
		double longitudeDistance = longitude - memberLongitude;

		// 좌표 -> km
		double distance = convertCoordinateToKm(latitudeDistance, longitudeDistance);

		return distance <= (length);
	}

	private double convertCoordinateToKm(double latitudeDistance, double longitudeDistance) {
		// 위도 35~38도(한국) 기준으로
		// 대략 위도는 1도당 111km, 경도는 1도당 89km
		double latitudeKm = 111 * latitudeDistance;
		double longitudeKm = 89 * longitudeDistance;

		return Math.sqrt((latitudeKm * latitudeKm) + (longitudeKm * longitudeKm));
	}

	@Transactional
	public Emergency saveEmergency(String receiverId, EmergencyRequestDTO dto) {
		// Emergency table에 저장
		Member member = memberService.findMemberById(dto.senderId());
		Child child = memberService.findChildByChildName(dto.childName());
		String content = "피보호자 이름 : " + dto.childName();

		Emergency emergency = dto.dtoToDomain(member, child, content);
		emergencyRepository.save(emergency);

		EmergencyReceiver emergencyReceiver = EmergencyReceiver.builder()
			.emergency(emergency)
			.emergencyReceiverId(receiverId)
			.build();
		emergencyReceiverRepository.save(emergencyReceiver);

		return emergency;
	}

	@Transactional
	public boolean sendNotificationTo(String receiverId, Emergency emergency) {
		FCMNotificationDTO message = makeMessage(receiverId, emergency);
		return fcmService.SendNotificationByToken(message);
	}

	private FCMNotificationDTO makeMessage(String receiverId, Emergency emergency) {
		return FCMNotificationDTO.of(
			receiverId, emergency.getTitle(), emergency.getContent()
		);
	}

	public List<Emergency> getSentEmergency(String memberId) {
		Member foundMember = memberService.findMemberById(memberId);
		return emergencyRepository.findAllBySenderId(foundMember);
	}

	public List<Emergency> getReceivedEmergency(String memberId) {
		List<Emergency> result = new ArrayList<>();
		List<EmergencyReceiver> foundEmergencyList = emergencyReceiverRepository.findAllByReceiverId(memberId);

		for (EmergencyReceiver received : foundEmergencyList) {
			result.add(received.getEmergency());
		}
		return result;
	}

	@Transactional
	public boolean writeComment(CommentRequestDTO commentRequestDTO) {
		Member foundMember = memberService.findMemberById(commentRequestDTO.commentatorId());
		Emergency foundEmergency = getEmergencyDetail(commentRequestDTO.emergencyId());

		Comment comment = Comment.builder()
			.commentator(foundMember)
			.emergency(foundEmergency)
			.comment(commentRequestDTO.commentContent())
			.build();

		commentRepository.save(comment);
		foundEmergency.commentList.add(comment);

		return true;
	}


	@Transactional
	public Emergency getEmergencyDetail(String emergencyId) {
		return emergencyRepository.findById(Long.valueOf(emergencyId))
			.orElseThrow(() -> new NoSuchElementException("No Such Member's emergency"));
	}

	@Transactional
	public List<Comment> getCommentOfEmergency(String emergencyId) {
		Emergency emergencyDetail = getEmergencyDetail(emergencyId);

		return commentRepository.findAllByEmergency(emergencyDetail);
	}

	@Transactional
	public void deleteComment(String commentId) {
		Comment foundComment = commentRepository.findById(Long.valueOf(commentId))
			.orElseThrow(() -> new NoSuchElementException("No Such Comment"));

		commentRepository.delete(foundComment);
	}

	public HashMap<String, FindNotificationResponse> addEmergencyList(List<Emergency> sentEmergencyList) {
		HashMap<String, FindNotificationResponse> result = new HashMap<>();

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
