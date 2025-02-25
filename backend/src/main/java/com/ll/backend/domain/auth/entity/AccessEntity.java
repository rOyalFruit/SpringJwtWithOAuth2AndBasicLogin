package com.ll.backend.domain.auth.entity;

import com.ll.backend.global.jwt.AuthConstants;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.concurrent.TimeUnit;

@Getter
@Setter
@RedisHash(value = "accessToken")
public class AccessEntity {

    @Id
    @Column(length = 300)
    private String access;

    private String username;

    private String expiration;

    @TimeToLive(unit = TimeUnit.MILLISECONDS)
    private Long timeToLive = AuthConstants.ACCESS_TOKEN_EXPIRATION;
}
