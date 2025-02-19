package com.ll.backend.dto;

import java.util.Map;

public class KakaoResponse implements OAuth2Response{

    private final Map<String, Object> attribute;
    private final Map<String, Object> properties;

    public KakaoResponse(Map<String, Object> attribute) {
        this.attribute = attribute;
        this.properties = (Map<String, Object>) this.attribute.get("properties");
    }


    @Override
    public String getProvider() {

        return "kakao";
    }

    @Override
    public String getProviderId() {
        System.out.println("attribute = " + attribute);
        return attribute.get("id").toString();
    }

    @Override
    public String getEmail() {

        return "비즈앱 전환 후 이용 가능";
    }

    @Override
    public String getName() {
        return properties.get("nickname").toString();
    }

    @Override
    public String getProfileImageUrl() {
        return properties.get("profile_image").toString();
    }
}