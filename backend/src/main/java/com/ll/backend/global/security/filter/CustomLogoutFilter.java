package com.ll.backend.global.security.filter;

import com.ll.backend.domain.auth.service.AccessTokenService;
import com.ll.backend.domain.auth.service.RefreshTokenService;
import com.ll.backend.global.exception.auth.AuthException;
import com.ll.backend.global.exception.auth.LogoutException;
import com.ll.backend.global.jwt.AuthConstants;
import com.ll.backend.global.jwt.JwtUtil;
import com.ll.backend.global.util.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class CustomLogoutFilter extends GenericFilterBean {

    private final JwtUtil jwtUtil;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!isLogoutRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String authorizationHeader = request.getHeader(AuthConstants.AUTHORIZATION);
            String accessToken = jwtUtil.extractAccessToken(authorizationHeader);
            String refreshToken = jwtUtil.extractRefreshToken(request.getCookies());

            jwtUtil.validateRefreshToken(refreshToken);
            // 로그아웃 요청 시점에 엑세스 토큰이 만료되었을 수 있으므로 엑세스 토큰 검증은 생략.
            // jwtUtil.validateAccessToken(accessToken);

            processLogout(accessToken, refreshToken);
            response.addCookie(CookieUtil.createExpiredCookie(AuthConstants.REFRESH_TOKEN));
        } catch (AuthException e) {
            request.setAttribute("errorCode", e.getErrorCode());
            throw new LogoutException();
        }
    }

    private boolean isLogoutRequest(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String requestMethod = request.getMethod();

        return requestUri.matches("^\\/logout$") && requestMethod.equals("POST");
    }

    private void processLogout(String accessToken, String refreshToken) {
        try {
            accessTokenService.deleteAccessToken(accessToken);
            refreshTokenService.deleteRefreshToken(refreshToken);
        } catch (Exception e) {
            log.error("Refresh 토큰 삭제 중 오류 발생: {}", e.getMessage());
            throw new LogoutException();
        }
    }
}