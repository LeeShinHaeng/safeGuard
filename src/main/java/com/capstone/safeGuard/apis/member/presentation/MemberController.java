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
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.GetIdRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.HelperRemoveRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.LoginRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.MemberRegisterRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.SignUpRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.UpdateMemberNameRequest;
import com.capstone.safeGuard.apis.member.presentation.request.updatecoordinate.CoordinateRequest;
import com.capstone.safeGuard.apis.member.presentation.request.updatecoordinate.UpdateCoordinate;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

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

	@GetMapping("/login")
	public String showLoginForm() {
		return "login";
	}

	@PostMapping("/login")
	public ResponseEntity<Map<String, String>> login(@Validated @RequestBody LoginRequest dto,
													 BindingResult bindingResult,
													 HttpServletResponse response,
													 HttpServletRequest request) {
		log.info(dto.editTextID(), dto.loginType());

		Map<String, String> result = new HashMap<>();

		if (bindingResult.hasErrors()) {
			return addBindingError(result);
		}

		// Member 타입으로 로그인 하는 경우
		if (dto.loginType().equals(LoginType.Member.toString())) {
			Member memberLogin = memberService.memberLogin(dto);

			// member가 존재하는 경우 token을 전달
			TokenInfo tokenInfo = memberService.generateTokenOfMember(memberLogin);
			storeTokenInBody(response, result, tokenInfo);
			result.put("Type", "Member");
		}
		// Child 타입으로 로그인 하는 경우
		else {
			Child childLogin = childService.childLogin(dto);

			// child가 존재하는 경우 token을 전달
			TokenInfo tokenInfo = memberService.generateTokenOfChild(childLogin);
			storeTokenInBody(response, result, tokenInfo);

			HttpSession session = request.getSession();
			session.setAttribute("childName", childLogin.getChildName());
			result.put("Type", "Child");
		}
		return addOkStatus(result);
	}

	private void storeTokenInBody(HttpServletResponse response, Map<String, String> result, TokenInfo tokenInfo) {
		response.setHeader("Authorization", tokenInfo.accessToken());
		// 생성한 토큰을 저장
		jwtService.storeToken(tokenInfo);
		result.put("authorization", tokenInfo.accessToken());
		result.put("status", "200");
	}

	@GetMapping("/signup")
	public String showMemberSignUpForm() {
		return "signup";
	}

	@PostMapping(value = "/signup", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> memberSignUp(@Validated @RequestBody SignUpRequest dto,
															BindingResult bindingResult) {
		log.info(dto.inputID());
		log.info(dto.inputName());

		HashMap<String, String> result = new HashMap<>();

		String errorMessage = memberUtil.validateBindingError(bindingResult);
		if (errorMessage != null) {
			return addErrorStatus(result);
		}

		memberService.signup(dto);
		return addOkStatus(result);
	}

	@GetMapping("/memberremove")
	public String showMemberRemoveForm() {
		return "login";
	}

	@PostMapping("/memberremove")
	public ResponseEntity<?> memberRemove(@Validated @RequestBody MemberIdRequest dto, BindingResult bindingResult) {

		String errorMessage = memberUtil.validateBindingError(bindingResult);
		if (errorMessage != null) {
			return ResponseEntity.badRequest().body(errorMessage);
		}

		String memberId = dto.memberId();
		memberService.memberRemove(memberId);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/childsignup")
	public String showChildSignUpForm() {
		return "group";
	}

	@PostMapping(value = "/childsignup", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> childSignUp(@Validated @RequestBody ChildRegisterRequest childDto,
											  BindingResult bindingResult) {
		log.info("childSignup 실행");

		String errorMessage = memberUtil.validateBindingError(bindingResult);
		if (errorMessage != null) {
			return ResponseEntity.badRequest().body(errorMessage);
		}

		childService.childSignUp(childDto);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/childremove")
	public String showChildRemoveForm() {
		return "group";
	}

	@PostMapping("/childremove")
	public ResponseEntity<String> childRemove(@Validated @RequestBody Map<String, String> requestBody,
											  BindingResult bindingResult) {

		String errorMessage = memberUtil.validateBindingError(bindingResult);
		if (errorMessage != null) {
			return ResponseEntity.badRequest().body(errorMessage);
		}

		String childName = requestBody.get("childName");
		childService.childRemove(childName);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/addhelper")
	public ResponseEntity<String> addHelper(@Validated @RequestBody MemberRegisterRequest memberRegisterRequest,
											BindingResult bindingResult) {
		String errorMessage = memberUtil.validateBindingError(bindingResult);
		if (errorMessage != null) {
			return ResponseEntity.badRequest().body(errorMessage);
		}
		memberUtil.addHelper(memberRegisterRequest);

		return ResponseEntity.ok().build();
	}

	@PostMapping("/return-nickname")
	public ResponseEntity<String> returnNickname(@Validated @RequestBody GetIdRequest dto,
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


	@PostMapping("/helperremove")
	public ResponseEntity<String> helperRemove(@Validated @RequestBody HelperRemoveRequest dto,
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
	public List<Child> showChildList(@Validated @RequestBody Map<String, String> requestBody) {

		String memberId = requestBody.get("memberId");

		log.info(memberId + "의 자식 리스트 반환 ");
		List<Child> childList = childService.getChildList(memberId);
		if (childList == null) {
			log.info("NULL");
		}

		return childList;
	}


	@GetMapping("/member-logout")
	public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
		Map<String, String> result = new HashMap<>();
		String requestToken = request.getHeader("Authorization");

		jwtService.findByToken(requestToken);
		memberService.logout(requestToken);
		return addOkStatus(result);
	}

	@PostMapping("/find-member-id")
	public ResponseEntity<Map<String, String>> findMemberId(@Valid @RequestBody FindMemberIdRequest dto,
															BindingResult bindingResult) {
		Map<String, String> result = new HashMap<>();

		if (bindingResult.hasErrors()) {
			return addBindingError(result);
		}

		String memberId = memberService.findMemberId(dto);

		result.put("status", "200");
		result.put("memberId", memberId);

		return ResponseEntity.ok().body(result);
	}

	// 비밀번호 확인을 위한 이메일 인증 1
	// 인증번호 전송
	@PostMapping("/verification-email-request")
	public ResponseEntity<Map<String, String>> verificationEmailRequest(@RequestBody EmailRequest dto) {
		Map<String, String> result = new HashMap<>();
		mailService.sendCodeToEmail(dto.inputId());
		return addOkStatus(result);
	}

	// 비밀번호 확인을 위한 이메일 인증 2
	// 인증번호 확인
	@PostMapping("/verification-email")
	public ResponseEntity<Map<String, String>> verificationEmail(@RequestBody VerificationEmailRequest dto) {
		Map<String, String> result = new HashMap<>();
		if (!mailService.verifiedCode(dto.inputId(), dto.inputCode())) {
			// 코드가 틀렸다는 메시지와 함께 다시 입력하는 곳으로 리다이렉트
			return addErrorStatus(result);
		}
		// 비밀번호 재설정 팝업 or 리다이렉트

		return addOkStatus(result);
	}

	// 비밀번호 확인을 위한 이메일 인증 3
	@PostMapping("/reset-member-password")
	public ResponseEntity<Map<String, String>> resetMemberPassword(@RequestBody ResetPasswordRequest dto) {
		Map<String, String> result = new HashMap<>();
		memberService.resetMemberPassword(dto);
		return addOkStatus(result);
	}

	@PostMapping("/find-child-list")
	public ResponseEntity<Map<String, String>> findChildNameList(@Validated @RequestBody MemberIdRequest dto) {
		Map<String, String> childList = getChildList(dto.memberId());
		return addOkStatus(childList);
	}

	@PostMapping("/find-parenting-helping-list")
	public ResponseEntity<Map<String, Map<String, String>>> findParentingAndHelpingList(@Validated @RequestBody MemberIdRequest dto) {
		Map<String, Map<String, String>> result = new HashMap<>();
		result.put("Parenting", getChildList(dto.memberId()));
		result.put("Helping", getHelpingList(dto.memberId()));

		return ResponseEntity.ok().body(result);
	}

	@PostMapping("/find-helping-list")
	public ResponseEntity<Map<String, Map<String, String>>> findHelpingList(@Validated @RequestBody MemberIdRequest dto) {
		Map<String, Map<String, String>> result = new HashMap<>();
		result.put("Helping", getHelpingList(dto.memberId()));

		return ResponseEntity.ok().body(result);
	}

	@PostMapping("/chose-child-form")
	public ResponseEntity<Map<String, String>> choseChildForm(@RequestBody MemberIdRequest dto) {
		Map<String, String> childList = getChildList(dto.memberId());

		return addOkStatus(childList);
	}

	@PostMapping("/chose-child")
	public ResponseEntity<Map<String, String>> choseChildToChangePassword(@RequestBody ResetPasswordRequest dto) {
		Map<String, String> result = new HashMap<>();
		childService.resetChildPassword(dto);
		return addOkStatus(result);
	}

	@PostMapping("/update-coordinate")
	public ResponseEntity<Map<String, String>> updateCoordinate(@RequestBody UpdateCoordinate dto) {
		Map<String, String> result = new HashMap<>();

		if (dto.type().equals("Member")) {
			coordinateService.updateMemberCoordinate(dto.id(), dto.latitude(), dto.longitude());
			batteryService.setMemberBattery(dto.id(), dto.battery());
			return addOkStatus(result);
		}
		coordinateService.updateChildCoordinate(dto.id(), dto.latitude(), dto.longitude());
		batteryService.setChildBattery(dto.id(), dto.battery());
		noticeService.sendNotice(dto.id());
		return addOkStatus(result);

	}

	@PostMapping("/return-coordinate")
	public ResponseEntity<Map<String, Double>> returnCoordinate(@RequestBody CoordinateRequest dto) {
		Map<String, Double> coordinates;

		if (dto.type().equals("Member")) {
			int memberBatteryValue = batteryService.getMemberBattery(dto.id());
			coordinates = coordinateService.getMemberCoordinate(dto.id());

			coordinates.put("battery", (memberBatteryValue * 1.0));
			return ResponseEntity.ok(coordinates);
		} else if (dto.type().equals("Child")) {
			int childBatteryValue = batteryService.getChildBattery(dto.id());
			coordinates = coordinateService.getChildCoordinate(dto.id());

			coordinates.put("battery", (childBatteryValue * 1.0));
			noticeService.sendNotice(dto.id());
			return ResponseEntity.ok(coordinates);
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	}

	@PostMapping("/duplicate-check-member")
	public ResponseEntity<Map<String, String>> duplicateCheckMember(@RequestBody GetIdRequest dto) {
		Map<String, String> result = new HashMap<>();
		if (memberUtil.isPresent(dto.id(), true)) {
			return addErrorStatus(result);
		}

		return addOkStatus(result);
	}

	@PostMapping("/duplicate-check-child")
	public ResponseEntity<Map<String, String>> duplicateCheckChild(@RequestBody GetIdRequest dto) {
		Map<String, String> result = new HashMap<>();
		if (memberUtil.isPresent(dto.id(), false)) {
			return addErrorStatus(result);
		}

		return addOkStatus(result);
	}

	@PostMapping("/add-parent")
	public ResponseEntity<Map<String, String>> addParent(@RequestBody MemberRegisterRequest dto) {
		Map<String, String> result = new HashMap<>();

		Member foundMember = memberUtil.findMemberById(dto.parentId());

		Child foundChild = memberUtil.findChildByName(dto.childName());
		if (foundChild == null) {
			return addErrorStatus(result);
		}

		memberUtil.addParent(foundMember.getMemberId(), foundChild.getChildName());
		return addOkStatus(result);
	}

	@Transactional
	@PostMapping("/find-member-by-child")
	public ResponseEntity<Map<String, Map<String, String>>> findMemberByChild(@RequestBody GetIdRequest dto) {
		Map<String, Map<String, String>> result = new HashMap<>();
		Child foundChild = memberUtil.findChildByName(dto.id());
		if (foundChild == null) {
			return ResponseEntity.status(400).build();
		}

		Map<String, String> memberMap1 = new HashMap<>();
		List<Parenting> parentingList = foundChild.getParentingList();
		if (parentingList != null) {
			for (int i = 0; i < parentingList.size(); i++) {
				memberMap1.put(String.valueOf(i + 1),
					parentingList.get(i).getParent().getMemberId());
			}
		}
		result.put("Parenting", memberMap1);


		Map<String, String> memberMap2 = new HashMap<>();
		List<Helping> helpingList = foundChild.getHelpingList();
		if (helpingList != null) {
			for (int i = 0; i < helpingList.size(); i++) {
				memberMap2.put(String.valueOf(i + 1),
					helpingList.get(i).getHelper().getMemberId());
			}
		}
		result.put("Helping", memberMap2);

		return ResponseEntity.ok().body(result);
	}

	@PostMapping("/update-nickname")
	public ResponseEntity<Map<String, String>> updateNickName(@RequestBody UpdateMemberNameRequest dto) {
		Map<String, String> result = new HashMap<>();
		memberService.updateMemberName(dto);

		return addOkStatus(result);
	}


	private Map<String, String> getChildList(String memberId) {
		Map<String, String> result = new HashMap<>();

		ArrayList<String> childList;
		try {
			childList = memberUtil.findChildList(memberId);
		} catch (NoSuchElementException e) {
			return new HashMap<>();
		}
		if (childList != null) {
			for (int i = 0; i < childList.size(); i++) {
				result.put(String.valueOf(i + 1), childList.get(i));
			}
		}

		return result;
	}

	private Map<String, String> getHelpingList(String memberId) {
		Map<String, String> result = new HashMap<>();

		ArrayList<String> childList;
		try {
			childList = memberUtil.findHelpingList(memberId);
		} catch (NoSuchElementException e) {
			return null;
		}
		if (childList != null) {
			for (int i = 0; i < childList.size(); i++) {
				result.put(String.valueOf(i + 1), childList.get(i));
			}
		}

		return result;
	}

	private static ResponseEntity<Map<String, String>> addOkStatus(Map<String, String> result) {
		result.put("status", "200");
		return ResponseEntity.ok().body(result);
	}

	private static ResponseEntity<Map<String, String>> addErrorStatus(Map<String, String> result) {
		result.put("status", "400");
		return ResponseEntity.status(400).body(result);
	}

	private static ResponseEntity<Map<String, String>> addBindingError(Map<String, String> result) {
		result.put("status", "403");
		return ResponseEntity.status(403).body(result);
	}
}
