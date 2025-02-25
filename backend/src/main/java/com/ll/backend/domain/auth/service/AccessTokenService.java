package com.ll.backend.domain.auth.service;

import com.ll.backend.domain.auth.entity.AccessEntity;
import com.ll.backend.global.jwt.AuthConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AccessTokenService {

    private final CacheManager cacheManager;

    @Cacheable(value = "accessToken", key = "#accessToken")
    public AccessEntity saveAccessToken(String username, String accessToken) {
        AccessEntity accessEntity = new AccessEntity();
        accessEntity.setUsername(username);
        accessEntity.setAccess(accessToken);
        accessEntity.setExpiration(new Date(System.currentTimeMillis() + AuthConstants.ACCESS_TOKEN_EXPIRATION).toString());
        return accessEntity;
    }

    @CacheEvict(value = "accessToken", key = "#accessToken")
    public void deleteAccessToken(String accessToken) {
        Cache cache = cacheManager.getCache("accessToken");
        if (cache != null) {
            cache.evict(accessToken);
        }
    }

    public AccessEntity getAccessEntity(String accessToken) {
        Cache cache = cacheManager.getCache("accessToken");
        if (cache != null) {
            return cache.get(accessToken, AccessEntity.class);
        }
        return null;
    }
}