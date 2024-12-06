package com.capstone.safeGuard.apis.member.application;

import com.capstone.safeGuard.domain.member.domain.EmailAuthCode;
import com.capstone.safeGuard.domain.member.domain.Member;
import com.capstone.safeGuard.domain.member.infrastructure.EmailAuthCodeRepository;
import com.capstone.safeGuard.domain.member.infrastructure.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MailService {
	private final MemberRepository memberRepository;
	private final MailUtil mailUtil;
	private final EmailAuthCodeRepository emailAuthCodeRepository;

	private static final int emailAuthCodeDuration = 1800; // 30 * 60 * 1000 == 30분

	public void sendCodeToEmail(String memberId) {
		Member foundMember = findMemberById(memberId);

		String address = foundMember.getEmail();
		String title = "SafeGuard 이메일 인증 번호";
		String authCode = createCode();

		mailUtil.sendEmail(address, title, authCode);
		Optional<EmailAuthCode> foundCode = emailAuthCodeRepository.findById(memberId);
		foundCode.ifPresent(emailAuthCodeRepository::delete);
		emailAuthCodeRepository.save(new EmailAuthCode(address, authCode, LocalDateTime.now()));
	}

	private String createCode() {
		int length = 6;
		Random random = new Random();
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < length; i++) {
			builder.append(random.nextInt(10));
		}

		return builder.toString();
	}

	public boolean verifiedCode(String memberId, String authCode) {
		Member foundMember = findMemberById(memberId);

		Optional<EmailAuthCode> foundCode = emailAuthCodeRepository.findById(foundMember.getEmail());
		if (foundCode.isEmpty()) {
			return false;
		}

		if (Duration.between(foundCode.get().getCreatedAt(), LocalDateTime.now()).getSeconds()
			> emailAuthCodeDuration) {
			return false;
		}

		return authCode.equals(foundCode.get().getAuthCode());
	}

	public Member findMemberById(String memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new RuntimeException("Member not found"));
	}
}
