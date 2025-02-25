package com.ll.backend.global.security.oauth2.dto;

public record OAuth2UserInfoDto(

        String role,

        String name,

        String username,

        String profileImageUrl
) {
}
