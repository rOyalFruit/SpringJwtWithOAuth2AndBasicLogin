package com.ll.backend.jwt;

import com.ll.backend.global.exception.auth.AuthException;
import com.ll.backend.global.exception.auth.LogoutException;
import com.ll.backend.global.exception.auth.token.ExpiredTokenException;
import com.ll.backend.global.exception.auth.token.InvalidTokenException;
import com.ll.backend.global.exception.auth.token.TokenNotFoundException;
import com.ll.backend.service.RefreshTokenService;
import com.ll.backend.util.CookieUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
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
            String refreshToken = extractRefreshToken(request);
            validateRefreshToken(refreshToken);
            processLogout(refreshToken);
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

    private String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String refreshToken = CookieUtil.extractCookieValue(cookies, AuthConstants.REFRESH_TOKEN);

        if (refreshToken == null) {
            throw new TokenNotFoundException();
        }

        return refreshToken;
    }

    private void validateRefreshToken(String refreshToken) {
        try {
            jwtUtil.isExpired(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new ExpiredTokenException();
        }

        String category = jwtUtil.getCategory(refreshToken);
        if (!category.equals(AuthConstants.REFRESH_TOKEN)) {
            throw new InvalidTokenException();
        }

        if (!refreshTokenService.existsByRefreshToken(refreshToken)) {
            throw new InvalidTokenException();
        }
    }

    private void processLogout(String refreshToken) {
        try {
            refreshTokenService.deleteRefreshToken(refreshToken);
        } catch (Exception e) {
            log.error("Refresh 토큰 삭제 중 오류 발생: {}", e.getMessage());
            throw new LogoutException();
        }
    }
}