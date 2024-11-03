package com.capstone.safeGuard.apis.member.application;

import com.capstone.safeGuard.domain.member.domain.JwtToken;
import com.capstone.safeGuard.apis.member.presentation.response.TokenInfo;
import com.capstone.safeGuard.domain.member.infrastructure.JwtTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {
    private final JwtTokenRepository jwtTokenRepository;

    public void storeToken(TokenInfo tokenInfo) {
        jwtTokenRepository.save(
                JwtToken.builder()
                        .grantType(tokenInfo.getGrantType())
                        .accessToken(tokenInfo.getAccessToken())
                        .refreshToken(tokenInfo.getRefreshToken()).build()
        );
    }


    @Transactional
    public void toBlackList(String accessToken) {
        Optional<JwtToken> findToken = jwtTokenRepository.findByAccessToken(accessToken);

        if (findToken.isEmpty() || findToken.get().isBlackList()) {
            throw new NoSuchElementException("Access token does not exist");
        }

        findToken.get().setBlackList(true);
    }

    public JwtToken findByToken(String token) {
        Optional<JwtToken> findToken = jwtTokenRepository.findByAccessToken(token);

        log.info("{}", token.equals(findToken.get().getAccessToken()));

        if (findToken.isEmpty() || findToken.get().isBlackList()) {
            throw new NoSuchElementException();
        }
        return findToken.get();
    }
}
