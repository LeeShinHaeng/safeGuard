package com.capstone.safeGuard.apis.member.application;

import com.capstone.safeGuard.apis.member.presentation.request.findidandresetpw.FindMemberIdRequest;
import com.capstone.safeGuard.apis.member.presentation.request.findidandresetpw.ResetPasswordRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.LoginRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.SignUpRequest;
import com.capstone.safeGuard.apis.member.presentation.request.signupandlogin.UpdateMemberNameRequest;
import com.capstone.safeGuard.apis.member.presentation.response.TokenInfo;
import com.capstone.safeGuard.domain.comment.domain.Comment;
import com.capstone.safeGuard.domain.comment.infrastructure.CommentRepository;
import com.capstone.safeGuard.domain.file.infrastructure.MemberFileRepository;
import com.capstone.safeGuard.domain.member.domain.Authority;
import com.capstone.safeGuard.domain.member.domain.Child;
import com.capstone.safeGuard.domain.member.domain.Member;
import com.capstone.safeGuard.domain.member.infrastructure.MemberBatteryRepository;
import com.capstone.safeGuard.domain.member.infrastructure.MemberRepository;
import com.capstone.safeGuard.domain.notice.infrastructure.EmergencyRepository;
import com.capstone.safeGuard.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	private final CommentRepository commentRepository;
	private final MemberBatteryRepository memberBatteryRepository;
	private final EmergencyRepository emergencyRepository;
	private final MemberFileRepository memberFileRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final MemberUtil memberUtil;
	private final ChildService childService;

	@Transactional
	public Member memberLogin(LoginRequest dto) {
		Member member = memberUtil.findMemberById(dto.editTextID());
		findMemberWithAuthenticate(member, dto.editTextPW());

		// 같은 기기를 사용하는 멤버 모두 FCM 토큰 초기화
		String fcmToken = dto.fcmToken();
		List<Member> existFcmList = memberRepository.findAllByFcmToken(fcmToken);
		if (!existFcmList.isEmpty()) {
			for (Member existFcm : existFcmList) {
				existFcm.setFcmToken(null);
			}
		}
		member.setFcmToken(fcmToken);

		return member;
	}

	public void findMemberWithAuthenticate(Member foundMember, String rawPassword) {
		if (!passwordEncoder.matches(rawPassword, foundMember.getPassword())) {
			throw new RuntimeException("Password not match");
		}
	}

	public void signup(SignUpRequest dto) {
		if (memberRepository.existsById(dto.inputID())) {
			throw new RuntimeException("Member already exists");
		}

		String email = dto.inputEmail();
		if (checkEmailDuplicate(email)) {
			log.info("Email Duplicate");
			throw new RuntimeException("Email Duplicate");
		}

		String encoded = passwordEncoder.encode(dto.inputPW());
		Member member = Member.of(dto, encoded);
		memberRepository.save(member);
	}

	public boolean checkEmailDuplicate(String email) {
		return memberRepository.existsByEmail(email);
	}

	@Transactional
	public void memberRemove(String memberId) {
		Member member = memberUtil.findMemberById(memberId);
		cascadeMemberRemove(member);
		memberRepository.delete(member);
	}

	@Transactional
	public void cascadeMemberRemove(Member member) {
		ArrayList<String> childNameList = memberUtil.findChildList(member.getMemberId());
		if (childNameList != null) {
			for (String childName : childNameList) {
				childService.childRemove(childName);
			}
		}

		List<Comment> commented = member.getCommented();
		if (commented != null) {
			commentRepository.deleteAll(commented);
		}

		memberUtil.deleteParentingList(member.getParentingList());
		memberUtil.deleteHelpingList(member.getHelpingList());
		memberUtil.deleteEmergencyList(emergencyRepository.findAllBySenderId(member));

		memberFileRepository.findByMember(member)
			.ifPresent(memberFileRepository::delete);
		memberBatteryRepository.findByMemberId(member)
			.ifPresent(battery -> memberBatteryRepository.deleteById(battery.getMemberBatteryId()));
	}

	public void logout(String accessToken) {
		jwtService.toBlackList(accessToken);
	}

	public String findMemberId(FindMemberIdRequest dto) {
		Member foundMember = memberRepository.findByEmail(dto.email());

		if (foundMember == null || (!foundMember.getName().equals(dto.name()))) {
			throw new IllegalStateException("Member not found");
		}
		return foundMember.getMemberId();
	}

	@Transactional
	public void resetMemberPassword(ResetPasswordRequest dto) {
		Member foundMember = memberUtil.findMemberById(dto.id());
		foundMember.setPassword(passwordEncoder.encode(dto.newPassword()));
	}

	public ArrayList<Member> findAllMember() {
		return new ArrayList<>(memberRepository.findAll());
	}

	@Transactional
	public void updateMemberName(UpdateMemberNameRequest dto) {
		Member foundMember = memberUtil.findMemberById(dto.userID());
		foundMember.setName(dto.nickname());
	}

	public String getNicknameById(String memberId) {
		Optional<Member> foundMember = memberRepository.findById(memberId);
		return foundMember.map(Member::getName).orElse(null);
	}

	public TokenInfo generateTokenOfMember(Member member) {
		Authentication authentication
			= new UsernamePasswordAuthenticationToken(member.getMemberId(), member.getPassword(),
			Collections.singleton(new SimpleGrantedAuthority(Authority.ROLE_MEMBER.toString())));
		return jwtTokenProvider.generateToken(authentication);
	}

	public TokenInfo generateTokenOfChild(Child child) {
		Authentication authentication
			= new UsernamePasswordAuthenticationToken(child.getChildName(), child.getChildPassword(),
			Collections.singleton(new SimpleGrantedAuthority(Authority.ROLE_CHILD.toString())));
		return jwtTokenProvider.generateToken(authentication);
	}
}