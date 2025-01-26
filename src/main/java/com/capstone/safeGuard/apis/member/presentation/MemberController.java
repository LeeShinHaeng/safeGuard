package com.capstone.safeGuard.apis.member.presentation;

import com.capstone.safeGuard.apis.member.application.ChildService;
import com.capstone.safeGuard.apis.member.application.MailService;
import com.capstone.safeGuard.apis.member.application.MemberService;
import com.capstone.safeGuard.apis.member.application.MemberUtil;
import com.capstone.safeGuard.apis.member.presentation.request.findidandresetpw.EmailRequest;
import com.capstone.safeGuard.apis.member.presentation.request.findidandresetpw.FindMemberIdRequest;
import com.capstone.safeGuard.apis.member.presentation.request.findidandresetpw.MemberIdRequest;
import com.capstone.safeGuard.apis.member.presentation.request.findidandresetpw.ResetPasswordRequest;
import com.capstone.safeGuard.apis.member.presentation.request.findidandresetpw.VerificationEmailRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.ChildRemoveRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.GetIdRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.UpdateMemberNameRequest;
import com.capstone.safeGuard.apis.member.presentation.response.MemberIdResponse;
import com.capstone.safeGuard.apis.member.presentation.response.StatusResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MemberController {
	private final MemberService memberService;
	private final MailService mailService;
	private final ChildService childService;
	private final MemberUtil memberUtil;

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

	@PostMapping("/reset-child-password")
	public ResponseEntity<StatusResponse> choseChildToChangePassword(@RequestBody ResetPasswordRequest dto) {
		childService.resetChildPassword(dto);
		return ResponseEntity.ok(StatusResponse.of("200"));
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

	@PostMapping("/update-nickname")
	public ResponseEntity<StatusResponse> updateNickName(@RequestBody UpdateMemberNameRequest dto) {
		memberService.updateMemberName(dto);
		return ResponseEntity.ok(StatusResponse.of("200"));
	}
}
