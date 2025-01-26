package com.capstone.safeGuard.apis.member.presentation;

import com.capstone.safeGuard.apis.member.application.ChildService;
import com.capstone.safeGuard.apis.member.application.JwtService;
import com.capstone.safeGuard.apis.member.application.MemberService;
import com.capstone.safeGuard.apis.member.application.MemberUtil;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.ChildRegisterRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.LoginRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.SignUpRequest;
import com.capstone.safeGuard.apis.member.presentation.response.LoginResponse;
import com.capstone.safeGuard.apis.member.presentation.response.StatusResponse;
import com.capstone.safeGuard.apis.member.presentation.response.TokenInfo;
import com.capstone.safeGuard.domain.member.domain.Child;
import com.capstone.safeGuard.domain.member.domain.LoginType;
import com.capstone.safeGuard.domain.member.domain.Member;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {
	private final MemberService memberService;
	private final JwtService jwtService;
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

		memberService.signup(dto);
		return ResponseEntity.ok(StatusResponse.of("200"));
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

	@GetMapping("/member-logout")
	public ResponseEntity<StatusResponse> logout(HttpServletRequest request) {
		String requestToken = request.getHeader("Authorization");

		jwtService.findByToken(requestToken);
		memberService.logout(requestToken);
		return ResponseEntity.ok(StatusResponse.of("200"));
	}
}
