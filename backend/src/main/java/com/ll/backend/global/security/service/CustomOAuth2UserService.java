package com.ll.backend.global.security.service;

import com.ll.backend.domain.member.entity.Member;
import com.ll.backend.domain.member.repository.MemberRepository;
import com.ll.backend.global.security.oauth2.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        System.out.println(oAuth2User);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null;

        if (registrationId.equals("naver")) {
            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        } else if (registrationId.equals("google")) {
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        } else if (registrationId.equals("kakao")) {
            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        } else {
            return null;
        }

        //리소스 서버에서 발급 받은 정보로 사용자를 특정할 아이디값을 만듬
        String username = oAuth2Response.getProvider() + "__" + oAuth2Response.getProviderId();
        Member existData = memberRepository.findByUsername(username);

        if (existData == null) {

            Member member = Member.builder()
                    .username(username)
                    .email(oAuth2Response.getEmail())
                    .name(oAuth2Response.getName())
                    .role("ROLE_USER")
                    .build();

            OAuth2UserInfoDto userDTO = new OAuth2UserInfoDto();
            userDTO.setUsername(username);
            userDTO.setName(oAuth2Response.getName());
            userDTO.setRole(member.getRole());
            userDTO.setProfileImageUrl(oAuth2Response.getProfileImageUrl());

            memberRepository.save(member);

            return new CustomOAuth2User(userDTO);
        }
        else {
            Member updatedMember = Member.builder()
                    .id(existData.getId())
                    .username(existData.getUsername())
                    .password(existData.getPassword())
                    .email(oAuth2Response.getEmail())
                    .name(oAuth2Response.getName())
                    .role(existData.getRole())
                    .build();

            OAuth2UserInfoDto userDTO = new OAuth2UserInfoDto();
            userDTO.setUsername(existData.getUsername());
            userDTO.setName(oAuth2Response.getName());
            userDTO.setRole(existData.getRole());
            userDTO.setProfileImageUrl(oAuth2Response.getProfileImageUrl());

            memberRepository.save(updatedMember);

            return new CustomOAuth2User(userDTO);
        }
    }
}
