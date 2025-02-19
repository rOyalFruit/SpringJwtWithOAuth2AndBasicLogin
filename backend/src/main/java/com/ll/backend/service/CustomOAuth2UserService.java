package com.ll.backend.service;

import com.ll.backend.dto.*;
import com.ll.backend.entity.Member;
import com.ll.backend.repository.MemberRepository;
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
            // 응답: JSON 형식
            // {resultcode=00, message=success, response={id=123123123, name=유저네임}}
            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        } else if (registrationId.equals("google")) {
            // 응답: JSON 형식
            // {resultcode=00, message=success, id=123123123, name=유저네임}
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        } else if (registrationId.equals("kakao")) {
            // 응답: JSON 형식
            // {
            //  "id": "3927761805",
            //  "connected_at": "2025-02-19T02:56:52Z",
            //  "properties": {
            //    "nickname": ".",
            //    "profile_image": "http://k.kakaocdn.net/dn/QZ3mB/btsFQR2Tjlj/kbsKC8ltgFt7W2cK9ROje1/img_640x640.jpg",
            //    "thumbnail_image": "http://k.kakaocdn.net/dn/QZ3mB/btsFQR2Tjlj/kbsKC8ltgFt7W2cK9ROje1/img_110x110.jpg"
            //  },
            //  "kakao_account": {
            //    "profile_nickname_needs_agreement": false,
            //    "profile_image_needs_agreement": false,
            //    "profile": {
            //      "nickname": ".",
            //      "thumbnail_image_url": "http://k.kakaocdn.net/dn/QZ3mB/btsFQR2Tjlj/kbsKC8ltgFt7W2cK9ROje1/img_110x110.jpg",
            //      "profile_image_url": "http://k.kakaocdn.net/dn/QZ3mB/btsFQR2Tjlj/kbsKC8ltgFt7W2cK9ROje1/img_640x640.jpg",
            //      "is_default_image": false,
            //      "is_default_nickname": false
            //    }
            //  }
            //}
            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        } else {
            return null;
        }

        //리소스 서버에서 발급 받은 정보로 사용자를 특정할 아이디값을 만듬
        String username = oAuth2Response.getProvider() + "__" + oAuth2Response.getProviderId();
        Member existData = memberRepository.findByUsername(username);

        if (existData == null) {

            Member member = new Member();
            member.setUsername(username);
            member.setEmail(oAuth2Response.getEmail());
            member.setName(oAuth2Response.getName());
            member.setRole("ROLE_USER");

            MemberDto userDTO = new MemberDto();
            userDTO.setUsername(username);
            userDTO.setName(oAuth2Response.getName());
            userDTO.setRole(member.getRole());
            userDTO.setProfileImageUrl(oAuth2Response.getProfileImageUrl());

            memberRepository.save(member);

            return new CustomOAuth2User(userDTO);
        }
        else {

            existData.setEmail(oAuth2Response.getEmail());
            existData.setName(oAuth2Response.getName());

            MemberDto userDTO = new MemberDto();
            userDTO.setUsername(existData.getUsername());
            userDTO.setName(oAuth2Response.getName());
            userDTO.setRole(existData.getRole());
            userDTO.setProfileImageUrl(oAuth2Response.getProfileImageUrl());

            memberRepository.save(existData);

            return new CustomOAuth2User(userDTO);
        }
    }
}
