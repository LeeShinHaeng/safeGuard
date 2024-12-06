package com.capstone.safeGuard.apis.member.application;

import com.capstone.safeGuard.apis.member.presentation.response.TokenInfo;
import com.capstone.safeGuard.domain.member.domain.JwtToken;
import com.capstone.safeGuard.domain.member.infrastructure.JwtTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {
	private final JwtTokenRepository jwtTokenRepository;

	public void storeToken(TokenInfo tokenInfo) {
		jwtTokenRepository.save(
			JwtToken.builder()
				.grantType(tokenInfo.grantType())
				.accessToken(tokenInfo.accessToken())
				.refreshToken(tokenInfo.refreshToken())
				.build()
		);
	}

	@Transactional
	public void toBlackList(String accessToken) {
		JwtToken findToken = jwtTokenRepository.findByAccessToken(accessToken)
			.orElseThrow(() -> new RuntimeException("Token not found"));

		findToken.setBlackList(true);
	}

	public JwtToken findByToken(String token) {
		JwtToken foundToken = jwtTokenRepository.findByAccessToken(token)
			.orElseThrow(() -> new RuntimeException("Token not found"));

		log.info("{}", token.equals(foundToken.getAccessToken()));

		if (foundToken.isBlackList()) {
			throw new RuntimeException("Token not found");
		}
		return foundToken;
	}
}
