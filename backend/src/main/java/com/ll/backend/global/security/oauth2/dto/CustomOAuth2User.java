package com.ll.backend.global.security.oauth2.dto;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    private final OAuth2UserInfoDto OAuth2UserInfoDto;

    @Override
    public Map<String, Object> getAttributes() {

        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add((GrantedAuthority) OAuth2UserInfoDto::getRole);

        return collection;
    }

    @Override
    public String getName() {

        return OAuth2UserInfoDto.getName();
    }

    public String getUsername() {

        return OAuth2UserInfoDto.getUsername();
    }
}
