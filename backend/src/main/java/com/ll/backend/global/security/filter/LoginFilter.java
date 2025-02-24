package com.ll.backend.global.security.filter;

import com.ll.backend.global.jwt.AuthConstants;
import com.ll.backend.global.jwt.JwtUtil;
import com.ll.backend.domain.auth.service.RefreshTokenService;
import com.ll.backend.global.jwt.TokenInfo;
import com.ll.backend.global.security.dto.CustomUserDetails;
import com.ll.backend.global.exception.auth.AuthException;
import com.ll.backend.global.exception.auth.LoginException;
import com.ll.backend.global.exception.auth.token.TokenGenerationException;
import com.ll.backend.global.exception.base.ErrorCode;
import com.ll.backend.global.util.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collection;
import java.util.Iterator;

@Slf4j
@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            String username = obtainUsername(request);
            String password = obtainPassword(request);

            if (username.isBlank() || password.isBlank()) {
                request.setAttribute("errorCode", ErrorCode.INVALID_LOGIN);
                throw new LoginException();
            }

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(username, password, null);

            return authenticationManager.authenticate(authToken);
        } catch (AuthException e) {
            request.setAttribute("errorCode", e.getErrorCode());
            throw new LoginException();
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {

        try {
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            String username = customUserDetails.getUsername();

            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
            GrantedAuthority auth = iterator.next();
            String role = auth.getAuthority();

            String accessToken = jwtUtil.createToken(TokenInfo.accessToken(username, role).build());
            String refreshToken = jwtUtil.createToken(TokenInfo.refreshToken(username, role).build());

            //Refresh 토큰 저장
            refreshTokenService.saveRefreshToken(username, refreshToken);

            // HTTP 인증 방식은 RFC 7235 정의에 따라 아래 인증 헤더 형태를 가져야 함.(Bearer tokenValue)
            response.setHeader(AuthConstants.AUTHORIZATION, "Bearer " + accessToken);
            response.addCookie(CookieUtil.createAuthCookie(AuthConstants.REFRESH_TOKEN, refreshToken));
            response.setStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            throw new TokenGenerationException();
        }
    }

    //로그인 실패시 실행하는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        log.error("Login failed: {}", failed.getMessage());
        request.setAttribute("errorCode", ErrorCode.INVALID_LOGIN);
        throw new LoginException();
    }
}
