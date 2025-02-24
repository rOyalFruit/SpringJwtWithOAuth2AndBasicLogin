package com.ll.backend.global.security.handler;

import com.ll.backend.global.security.oauth2.dto.CustomOAuth2User;
import com.ll.backend.global.jwt.AuthConstants;
import com.ll.backend.global.jwt.JwtUtil;
import com.ll.backend.global.jwt.TokenInfo;
import com.ll.backend.domain.auth.service.RefreshTokenService;
import com.ll.backend.global.util.CookieUtil;
import jakarta.servlet.ServletException;
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
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        //OAuth2User
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        String username = customUserDetails.getUsername();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        String accessToken = jwtUtil.createToken(TokenInfo.accessToken(username, role).build());
        String refreshToken = jwtUtil.createToken(TokenInfo.refreshToken(username, role).build());

        refreshTokenService.saveRefreshToken(username, refreshToken);

        response.addCookie(CookieUtil.createAuthCookie(AuthConstants.AUTHORIZATION, accessToken));
        response.addCookie(CookieUtil.createAuthCookie(AuthConstants.REFRESH_TOKEN, refreshToken));
        // 엑세스 토큰은 헤더로 전송 후 사용자의 로컬 스토리지에, 리프레시 토큰은 쿠키로 저장해야 함.
        // response.addHeader로 발급을 진행하면 하이퍼 링크로 받을 수 없음.
        // 첫 발급 이후에는 헤더로 JWT를 이동 시킬 수 있으므로 아래 로직을 이용
        // 1. 로그인 성공 쿠키로 발급
        // 2. 프론트의 특정 페이지로 리디렉션을 보냄 (현재 단계)
        // 3. 프론트의 특정 페이지는 axios를 통해 쿠키를(credentials=true)를 가지고 다시 백엔드로 접근하여 헤더로 JWT를 받아옴
        // 4. 헤더로 받아온 JWT를 로컬 스토리지등에 보관하여 사용
        response.sendRedirect(AuthConstants.AUTH_SUCCESS_REDIRECT_URL);
    }
}
