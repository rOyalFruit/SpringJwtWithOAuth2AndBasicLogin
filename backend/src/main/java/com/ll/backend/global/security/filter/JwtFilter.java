package com.ll.backend.global.security.filter;

import com.ll.backend.domain.member.entity.Member;
import com.ll.backend.global.exception.auth.token.InvalidTokenException;
import com.ll.backend.global.jwt.AuthConstants;
import com.ll.backend.global.jwt.JwtUtil;
import com.ll.backend.global.security.dto.CustomUserDetails;
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

        String authorizationHeader = request.getHeader(AuthConstants.AUTHORIZATION);
        String accessToken = jwtUtil.extractAccessToken(authorizationHeader);

        if (accessToken != null) {
            jwtUtil.validateAccessToken(accessToken);
            Authentication authentication = createAuthentication(accessToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private Authentication createAuthentication(String token) {
        try {
            String username = jwtUtil.getUsername(token);
            String role = jwtUtil.getRole(token);

            Member member = Member.builder()
                    .username(username)
                    .role(role)
                    .build();

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
               path.startsWith("/h2-console") ||
               path.startsWith("/qr") ||
               path.startsWith("/receive-emails") ||
               path.equals("/") ||
               (path.startsWith("/join") && method.equals("POST"));
    }
}
