package com.ll.backend.domain.auth.entity;

import com.ll.backend.global.jwt.AuthConstants;
import jakarta.persistence.Column;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.concurrent.TimeUnit;

@RedisHash(value = "refreshToken")
public record RefreshEntity(
        @Id
        @Column(length = 300)
        String refresh,

        String username,

        String expiration,

        @TimeToLive(unit = TimeUnit.MILLISECONDS)
        Long timeToLive
) { // Custom constructor to set default value for timeToLive
    public RefreshEntity(String refresh, String username, String expiration) {
        this(refresh, username, expiration, AuthConstants.REFRESH_TOKEN_EXPIRATION);
    }
}


