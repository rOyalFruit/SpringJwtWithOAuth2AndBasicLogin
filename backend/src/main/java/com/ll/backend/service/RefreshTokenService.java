package com.ll.backend.service;

import com.ll.backend.entity.RefreshEntity;
import com.ll.backend.jwt.AuthConstants;
import com.ll.backend.repository.RefreshRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefreshTokenService {
    private final RefreshRepository refreshRepository;

    @Transactional
    public void saveRefreshToken(String username, String refreshToken) {
        RefreshEntity refreshEntity = new RefreshEntity();
        refreshEntity.setUsername(username);
        refreshEntity.setRefresh(refreshToken);
        refreshEntity.setExpiration(new Date(System.currentTimeMillis() + AuthConstants.REFRESH_TOKEN_EXPIRATION).toString());

        refreshRepository.save(refreshEntity);
    }

    @Transactional
    public void deleteRefreshToken(String refreshToken) {
        refreshRepository.deleteByRefresh(refreshToken);
    }

    public boolean existsByRefreshToken(String refreshToken) {
        return refreshRepository.existsByRefresh(refreshToken);
    }
}