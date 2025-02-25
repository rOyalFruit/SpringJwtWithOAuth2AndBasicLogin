package com.ll.backend.domain.auth.service;

import com.ll.backend.domain.auth.entity.RefreshEntity;
import com.ll.backend.domain.auth.repository.RefreshRepository;
import com.ll.backend.global.jwt.AuthConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshRepository refreshRepository;

    @Transactional
    public void saveRefreshToken(String username, String refreshToken) {
        RefreshEntity refreshEntity = new RefreshEntity(
                refreshToken,
                username,
                new Date(System.currentTimeMillis() + AuthConstants.REFRESH_TOKEN_EXPIRATION).toString()
        );

        refreshRepository.save(refreshEntity);
    }

    @Transactional
    public void deleteRefreshToken(String refreshToken) {
        refreshRepository.deleteById(refreshToken);
    }

    @Transactional(readOnly = true)
    public boolean existsByRefreshToken(String refreshToken) {
        return refreshRepository.existsById(refreshToken);
    }
}