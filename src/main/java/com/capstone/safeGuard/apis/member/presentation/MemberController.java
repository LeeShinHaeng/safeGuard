package com.capstone.safeGuard.apis.member.presentation;

import com.capstone.safeGuard.apis.map.application.CoordinateService;
import com.capstone.safeGuard.apis.member.application.BatteryService;
import com.capstone.safeGuard.apis.member.application.ChildService;
import com.capstone.safeGuard.apis.member.application.JwtService;
import com.capstone.safeGuard.apis.member.application.MailService;
import com.capstone.safeGuard.apis.member.application.MemberService;
import com.capstone.safeGuard.apis.member.application.MemberUtil;
import com.capstone.safeGuard.apis.member.presentation.request.findidandresetpw.EmailRequest;
import com.capstone.safeGuard.apis.member.presentation.request.findidandresetpw.FindMemberIdRequest;
import com.capstone.safeGuard.apis.member.presentation.request.findidandresetpw.MemberIdRequest;
import com.capstone.safeGuard.apis.member.presentation.request.findidandresetpw.ResetPasswordRequest;
import com.capstone.safeGuard.apis.member.presentation.request.findidandresetpw.VerificationEmailRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.ChildRegisterRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.ChildRemoveRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.GetIdRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.HelperRemoveRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.LoginRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.MemberRegisterRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.SignUpRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.UpdateMemberNameRequest;
import com.capstone.safeGuard.apis.member.presentation.request.updatecoordinate.CoordinateRequest;
import com.capstone.safeGuard.apis.member.presentation.request.updatecoordinate.UpdateCoordinate;
import com.capstone.safeGuard.apis.member.presentation.response.ChildListResponse;
import com.capstone.safeGuard.apis.member.presentation.response.ChildNameListResponse;
import com.capstone.safeGuard.apis.member.presentation.response.ChildResponse;
import com.capstone.safeGuard.apis.member.presentation.response.CoordinateAndBatteryResponse;
import com.capstone.safeGuard.apis.member.presentation.response.HelpingListResponse;
import com.capstone.safeGuard.apis.member.presentation.response.LoginResponse;
import com.capstone.safeGuard.apis.member.presentation.response.MemberIdResponse;
import com.capstone.safeGuard.apis.member.presentation.response.ParentingHelpingListResponse;
import com.capstone.safeGuard.apis.member.presentation.response.ParentingListResponse;
import com.capstone.safeGuard.apis.member.presentation.response.StatusResponse;
import com.capstone.safeGuard.apis.member.presentation.response.TokenInfo;
import com.capstone.safeGuard.apis.notice.application.NoticeService;
import com.capstone.safeGuard.domain.member.domain.Child;
import com.capstone.safeGuard.domain.member.domain.Helping;
import com.capstone.safeGuard.domain.member.domain.LoginType;
import com.capstone.safeGuard.domain.member.domain.Member;
import com.capstone.safeGuard.domain.member.domain.Parenting;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MemberController {
	private final MemberService memberService;
	private final JwtService jwtService;
	private final BatteryService batteryService;
	private final NoticeService noticeService;
	private final MailService mailService;
	private final CoordinateService coordinateService;
	private final ChildService childService;
	private final MemberUtil memberUtil;

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest dto,
											   HttpServletResponse response,
											   HttpServletRequest request) {
		log.info(dto.editTextID(), dto.loginType());

		// Member 타입으로 로그인 하는 경우
		if (dto.loginType().equals(LoginType.Member.toString())) {
			Member memberLogin = memberService.memberLogin(dto);

			// member가 존재하는 경우 token을 전달
			TokenInfo tokenInfo = memberService.generateTokenOfMember(memberLogin);
			response.setHeader("Authorization", tokenInfo.accessToken());
			return ResponseEntity.ok(LoginResponse.of(tokenInfo.accessToken(), "200", "Member"));
		}

		// Child 타입으로 로그인 하는 경우
		else {
			Child childLogin = childService.childLogin(dto);

			HttpSession session = request.getSession();
			session.setAttribute("childName", childLogin.getChildName());

			// child가 존재하는 경우 token을 전달
			TokenInfo tokenInfo = memberService.generateTokenOfChild(childLogin);
			response.setHeader("Authorization", tokenInfo.accessToken());
			return ResponseEntity.ok(LoginResponse.of(tokenInfo.accessToken(), "200", "Child"));
		}
	}

	@PostMapping(value = "/signup")
	public ResponseEntity<StatusResponse> memberSignUp(@RequestBody SignUpRequest dto) {
		log.info(dto.inputID());
		log.info(dto.inputName());

		HashMap<String, String> result = new HashMap<>();

		memberService.signup(dto);
		return ResponseEntity.ok(StatusResponse.of("200"));
	}

	@PostMapping("/member-remove")
	public ResponseEntity<String> memberRemove(@RequestBody MemberIdRequest dto, BindingResult bindingResult) {

		String errorMessage = memberUtil.validateBindingError(bindingResult);
		if (errorMessage != null) {
			return ResponseEntity.badRequest().body(errorMessage);
		}

		String memberId = dto.memberId();
		memberService.memberRemove(memberId);
		return ResponseEntity.ok().build();
	}

	@PostMapping(value = "/child-signup", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> childSignUp(@RequestBody ChildRegisterRequest childDto,
											  BindingResult bindingResult) {
		log.info("childSignup 실행");

		String errorMessage = memberUtil.validateBindingError(bindingResult);
		if (errorMessage != null) {
			return ResponseEntity.badRequest().body(errorMessage);
		}

		childService.childSignUp(childDto);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/child-remove")
	public ResponseEntity<String> childRemove(@RequestBody ChildRemoveRequest dto,
											  BindingResult bindingResult) {

		String errorMessage = memberUtil.validateBindingError(bindingResult);
		if (errorMessage != null) {
			return ResponseEntity.badRequest().body(errorMessage);
		}

		String childName = dto.childName();
		childService.childRemove(childName);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/add-helper")
	public ResponseEntity<String> addHelper(@RequestBody MemberRegisterRequest dto,
											BindingResult bindingResult) {
		String errorMessage = memberUtil.validateBindingError(bindingResult);
		if (errorMessage != null) {
			return ResponseEntity.badRequest().body(errorMessage);
		}
		memberUtil.addHelper(dto);

		return ResponseEntity.ok().build();
	}

	@PostMapping("/get-nickname")
	public ResponseEntity<String> returnNickname(@RequestBody GetIdRequest dto,
												 BindingResult bindingResult) {
		String errorMessage = memberUtil.validateBindingError(bindingResult);
		if (errorMessage != null) {
			return ResponseEntity.badRequest().body(errorMessage);
		}

		String nickname = memberService.getNicknameById(dto.id());
		if (nickname != null) {
			return ResponseEntity.ok().body(nickname);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("닉네임을 찾을 수 없습니다.");
		}
	}


	@PostMapping("/helper-remove")
	public ResponseEntity<String> helperRemove(@RequestBody HelperRemoveRequest dto,
											   BindingResult bindingResult) {
		String errorMessage = memberUtil.validateBindingError(bindingResult);
		if (errorMessage != null) {
			return ResponseEntity.badRequest().body(errorMessage);
		}

		memberUtil.helperRemove(dto);
		return ResponseEntity.ok().build();
	}

	//로그인한 멤버의 자식(그룹)들을 찾아서 반환
	@PostMapping("/group")
	public ResponseEntity<ChildListResponse> showChildList(@RequestBody MemberIdRequest dto) {
		String memberId = dto.memberId();

		log.info(memberId + "의 자식 리스트 반환 ");
		List<Child> childList = childService.getChildList(memberId);
		if (childList == null) {
			log.info("NULL");
			return ResponseEntity.ok(ChildListResponse.fromChildList(null));
		}

		List<ChildResponse> responses = new ArrayList<>();
		for (Child child : childList) {
			responses.add(ChildResponse.of(
				child.getId().toString(),
				child.getChildName(),
				child.getLastStatus()
			));
		}

		return ResponseEntity.ok(ChildListResponse.fromChildList(responses));
	}


	@GetMapping("/member-logout")
	public ResponseEntity<StatusResponse> logout(HttpServletRequest request) {
		String requestToken = request.getHeader("Authorization");

		jwtService.findByToken(requestToken);
		memberService.logout(requestToken);
		return ResponseEntity.ok(StatusResponse.of("200"));
	}

	@PostMapping("/find-member-id")
	public ResponseEntity<MemberIdResponse> findMemberId(@Valid @RequestBody FindMemberIdRequest dto) {
		String memberId = memberService.findMemberId(dto);
		return ResponseEntity.ok().body(MemberIdResponse.of("200", memberId));
	}

	// 비밀번호 확인을 위한 이메일 인증 1
	// 인증번호 전송
	@PostMapping("/verification-email-request")
	public ResponseEntity<StatusResponse> verificationEmailRequest(@RequestBody EmailRequest dto) {
		mailService.sendCodeToEmail(dto.inputId());
		return ResponseEntity.ok(StatusResponse.of("200"));
	}

	// 비밀번호 확인을 위한 이메일 인증 2
	// 인증번호 확인
	@PostMapping("/verification-email")
	public ResponseEntity<StatusResponse> verificationEmail(@RequestBody VerificationEmailRequest dto) {
		if (!mailService.verifiedCode(dto.inputId(), dto.inputCode())) {
			return ResponseEntity.ok(StatusResponse.of("400"));
		}

		return ResponseEntity.ok(StatusResponse.of("200"));
	}

	// 비밀번호 확인을 위한 이메일 인증 3
	@PostMapping("/reset-member-password")
	public ResponseEntity<StatusResponse> resetMemberPassword(@RequestBody ResetPasswordRequest dto) {
		memberService.resetMemberPassword(dto);
		return ResponseEntity.ok(StatusResponse.of("200"));
	}

	@PostMapping("/find-child-list")
	public ResponseEntity<ChildNameListResponse> findChildNameList(@RequestBody MemberIdRequest dto) {
		Map<String, String> childList = memberService.getChildList(dto.memberId());
		return ResponseEntity.ok(ChildNameListResponse.fromMap(childList));
	}

	@PostMapping("/find-parenting-helping-list")
	public ResponseEntity<ParentingHelpingListResponse> findParentingAndHelpingList(@RequestBody MemberIdRequest dto) {
		return ResponseEntity.ok().body(
			ParentingHelpingListResponse.from(
				memberService.getChildList(dto.memberId()),
				memberService.getHelpingList(dto.memberId())
			));
	}

	@PostMapping("/find-helping-list")
	public ResponseEntity<HelpingListResponse> findHelpingList(@RequestBody MemberIdRequest dto) {
		return ResponseEntity.ok().body(
			HelpingListResponse.from(
				memberService.getHelpingList(dto.memberId())
			)
		);
	}

	@PostMapping("/find-parenting-list")
	public ResponseEntity<ParentingListResponse> choseChildForm(@RequestBody MemberIdRequest dto) {
		return ResponseEntity.ok().body(
			ParentingListResponse.from(
				memberService.getChildList(dto.memberId())
			)
		);
	}

	@PostMapping("/chose-child")
	public ResponseEntity<StatusResponse> choseChildToChangePassword(@RequestBody ResetPasswordRequest dto) {
		childService.resetChildPassword(dto);
		return ResponseEntity.ok(StatusResponse.of("200"));
	}

	@PostMapping("/update-coordinate")
	public ResponseEntity<StatusResponse> updateCoordinate(@RequestBody UpdateCoordinate dto) {
		Map<String, String> result = new HashMap<>();

		if (dto.type().equals("Member")) {
			coordinateService.updateMemberCoordinate(dto.id(), dto.latitude(), dto.longitude());
			batteryService.setMemberBattery(dto.id(), dto.battery());
			return ResponseEntity.ok(StatusResponse.of("200"));
		}
		coordinateService.updateChildCoordinate(dto.id(), dto.latitude(), dto.longitude());
		batteryService.setChildBattery(dto.id(), dto.battery());
		noticeService.sendNotice(dto.id());
		return ResponseEntity.ok(StatusResponse.of("200"));

	}

	@PostMapping("/return-coordinate")
	public ResponseEntity<CoordinateAndBatteryResponse> returnCoordinate(@RequestBody CoordinateRequest dto) {
		Map<String, Double> coordinates;
		if (dto.type().equals("Member")) {
			int memberBatteryValue = batteryService.getMemberBattery(dto.id());
			coordinates = coordinateService.getMemberCoordinate(dto.id());
			coordinates.put("battery", (memberBatteryValue * 1.0));

			return ResponseEntity.ok(CoordinateAndBatteryResponse.fromMap(coordinates));
		} else if (dto.type().equals("Child")) {
			int childBatteryValue = batteryService.getChildBattery(dto.id());
			coordinates = coordinateService.getChildCoordinate(dto.id());
			coordinates.put("battery", (childBatteryValue * 1.0));

			noticeService.sendNotice(dto.id());
			return ResponseEntity.ok(CoordinateAndBatteryResponse.fromMap(coordinates));
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	}

	@PostMapping("/duplicate-check-member")
	public ResponseEntity<StatusResponse> duplicateCheckMember(@RequestBody GetIdRequest dto) {
		if (memberUtil.isPresent(dto.id(), true)) {
			return ResponseEntity.ok(StatusResponse.of("400"));
		}

		return ResponseEntity.ok(StatusResponse.of("200"));
	}

	@PostMapping("/duplicate-check-child")
	public ResponseEntity<StatusResponse> duplicateCheckChild(@RequestBody GetIdRequest dto) {
		if (memberUtil.isPresent(dto.id(), false)) {
			return ResponseEntity.ok(StatusResponse.of("400"));
		}

		return ResponseEntity.ok(StatusResponse.of("200"));
	}

	@PostMapping("/add-parent")
	public ResponseEntity<StatusResponse> addParent(@RequestBody MemberRegisterRequest dto) {
		Member foundMember = memberUtil.findMemberById(dto.parentId());
		Child foundChild = memberUtil.findChildByName(dto.childName());

		if (foundChild == null) {
			return ResponseEntity.ok(StatusResponse.of("400"));
		}

		memberUtil.addParent(foundMember.getMemberId(), foundChild.getChildName());
		return ResponseEntity.ok(StatusResponse.of("200"));
	}

	@PostMapping("/find-member-by-child")
	public ResponseEntity<ParentingHelpingListResponse> findMemberByChild(@RequestBody GetIdRequest dto) {
		Child foundChild = memberUtil.findChildByName(dto.id());
		if (foundChild == null) {
			return ResponseEntity.status(400).build();
		}

		Map<String, String> memberMap1 = new HashMap<>();
		List<Parenting> parentingList = foundChild.getParentingList();
		if (parentingList != null) {
			for (int i = 0; i < parentingList.size(); i++) {
				memberMap1.put(
					String.valueOf(i + 1),
					parentingList.get(i).getParent().getMemberId()
				);
			}
		}

		Map<String, String> memberMap2 = new HashMap<>();
		List<Helping> helpingList = foundChild.getHelpingList();
		if (helpingList != null) {
			for (int i = 0; i < helpingList.size(); i++) {
				memberMap2.put(
					String.valueOf(i + 1),
					helpingList.get(i).getHelper().getMemberId()
				);
			}
		}

		return ResponseEntity.ok().body(ParentingHelpingListResponse.from(memberMap1, memberMap2));
	}

	@PostMapping("/update-nickname")
	public ResponseEntity<StatusResponse> updateNickName(@RequestBody UpdateMemberNameRequest dto) {
		memberService.updateMemberName(dto);
		return ResponseEntity.ok(StatusResponse.of("200"));
	}
}
