package com.ll.backend.jwt;

import lombok.Builder;

@Builder
public record TokenInfo(
        String username,
        String role,
        String category,
        Long expirationTime
) {
    // access token 생성을 위한 정적 팩토리 메서드
    public static TokenInfoBuilder accessToken(String username, String role) {
        return TokenInfo.builder()
                .username(username)
                .role(role)
                .category(AuthConstants.ACCESS_TOKEN)
                .expirationTime(AuthConstants.ACCESS_TOKEN_EXPIRATION);
    }

    // refresh token 생성을 위한 정적 팩토리 메서드
    public static TokenInfoBuilder refreshToken(String username, String role) {
        return TokenInfo.builder()
                .username(username)
                .role(role)
                .category(AuthConstants.REFRESH_TOKEN)
                .expirationTime(AuthConstants.REFRESH_TOKEN_EXPIRATION);
    }
}
