package com.ll.backend.domain.auth.service;

import com.ll.backend.domain.auth.entity.AccessEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessTokenService {

    private final CacheManager cacheManager;

    @Cacheable(value = "accessToken", key = "#accessToken")
    public AccessEntity saveAccessToken(String username, String accessToken) {
        AccessEntity accessEntity = new AccessEntity();
        accessEntity.setUsername(username);
        accessEntity.setAccess(accessToken);
        return accessEntity;
    }

    @CacheEvict(value = "accessToken", key = "#accessToken")
    public void deleteAccessToken(String accessToken) {
        Cache cache = cacheManager.getCache("accessToken");
        if (cache != null) {
            cache.evict(accessToken);
        }
    }
}