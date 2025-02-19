package com.ll.backend.oauth2;

import com.ll.backend.dto.CustomOAuth2User;
import com.ll.backend.jwt.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        //OAuth2User
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        String username = customUserDetails.getUsername();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        String token = jwtUtil.createJwt(username, role, 60*60*60L);

        response.addCookie(createCookie("Authorization", token));
        // response.addHeader로 발급을 진행하면 하이퍼 링크로 받을 수 없음.
        // 첫 발급 이후에는 헤더로 JWT를 이동 시킬 수 있으므로 아래 로직을 이용
        // 1. 로그인 성공 쿠키로 발급
        // 2. 프론트의 특정 페이지로 리디렉션을 보냄 (현재 단계)
        // 3. 프론트의 특정 페이지는 axios를 통해 쿠키를(credentials=true)를 가지고 다시 백엔드로 접근하여 헤더로 JWT를 받아옴
        // 4. 헤더로 받아온 JWT를 로컬 스토리지등에 보관하여 사용
        response.sendRedirect("http://localhost:3000/auth-success");
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60*60*60);
        //cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }
}
