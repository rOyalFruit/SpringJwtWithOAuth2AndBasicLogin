package com.ll.backend.global.security.handler;

import com.ll.backend.domain.auth.service.AccessTokenService;
import com.ll.backend.domain.auth.service.RefreshTokenService;
import com.ll.backend.global.jwt.AuthConstants;
import com.ll.backend.global.jwt.JwtUtil;
import com.ll.backend.global.jwt.TokenInfo;
import com.ll.backend.global.security.oauth2.dto.CustomOAuth2User;
import com.ll.backend.global.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final AccessTokenService accessTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        // OAuth2User
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
        String username = customUserDetails.getUsername();
        String role = authentication.getAuthorities().stream()
                                    .findFirst()
                                    .map(GrantedAuthority::getAuthority)
                                    .orElseThrow(() -> new IllegalStateException("User has no roles"));

        String accessToken = jwtUtil.createToken(TokenInfo.accessToken(username, role).build());
        String refreshToken = jwtUtil.createToken(TokenInfo.refreshToken(username, role).build());

        // Save tokens to Redis
        refreshTokenService.saveRefreshToken(username, refreshToken);
        accessTokenService.saveAccessToken(username, accessToken);

        response.addCookie(CookieUtil.createAuthCookie(AuthConstants.AUTHORIZATION, accessToken));
        response.addCookie(CookieUtil.createAuthCookie(AuthConstants.REFRESH_TOKEN, refreshToken));

        // Redirect to success URL
        response.sendRedirect(AuthConstants.AUTH_SUCCESS_REDIRECT_URL);
    }
}
