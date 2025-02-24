package com.ll.backend.global.security.filter;

import com.ll.backend.global.jwt.AuthConstants;
import com.ll.backend.global.jwt.JwtUtil;
import com.ll.backend.global.security.dto.CustomUserDetails;
import com.ll.backend.domain.member.entity.Member;
import com.ll.backend.global.exception.auth.token.ExpiredTokenException;
import com.ll.backend.global.exception.auth.token.InvalidTokenException;
import com.ll.backend.global.exception.auth.token.TokenNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 인증이 필요없는 경로는 필터 검사에서 제외
        if (shouldSkipFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);

        if (token != null) {
            validateToken(token);
            Authentication authentication = createAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String token = request.getHeader(AuthConstants.AUTHORIZATION);

        if (token == null) {
            throw new TokenNotFoundException();
        }

        if (!token.startsWith("Bearer ")) {
            throw new InvalidTokenException();
        }

        return token.split(" ")[1];
    }

    private void validateToken(String token) {
        if (jwtUtil.isExpired(token)) {
            throw new ExpiredTokenException();
        }

        String category = jwtUtil.getCategory(token);
        if (!category.equals(AuthConstants.ACCESS_TOKEN)) {
            throw new InvalidTokenException();
        }
    }

    private Authentication createAuthentication(String token) {
        try {
            String username = jwtUtil.getUsername(token);
            String role = jwtUtil.getRole(token);

            Member member = new Member();
            member.setUsername(username);
            member.setRole(role);

            UserDetails userDetails = new CustomUserDetails(member);
            return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        } catch (Exception e) {
            throw new InvalidTokenException();
        }
    }

    private boolean shouldSkipFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        return path.startsWith("/login") ||           // 모든 로그인 관련 경로
               path.startsWith("/oauth2") ||          // 모든 OAuth2 관련 경로
               path.startsWith("/jwt") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.equals("/") ||
               (path.equals("/join") && method.equals("POST"));
    }
}
