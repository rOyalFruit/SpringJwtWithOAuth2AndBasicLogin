package com.ll.backend.domain.auth.entity;

import com.ll.backend.global.jwt.AuthConstants;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.concurrent.TimeUnit;

@RedisHash(value = "accessToken")
public record AccessEntity(
        @Id
        String access,

        String username,

        String expiration,

        @TimeToLive(unit = TimeUnit.MILLISECONDS)
        Long timeToLive
) {
    // Custom constructor to set default value for timeToLive
    public AccessEntity(String access, String username, String expiration) {
        this(access, username, expiration, AuthConstants.ACCESS_TOKEN_EXPIRATION);
    }
}
