package com.ll.backend.global.security.oauth2.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuth2UserInfoDto {

    private String role;

    private String name;

    private String username;

    private String profileImageUrl;
}
