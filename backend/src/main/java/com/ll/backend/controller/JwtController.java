package com.ll.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "JwtController", description = "JWT 관련 컨트롤러")
@SecurityRequirement(name = "bearerAuth")
@RestController
public class JwtController {

    @Operation(summary = "소셜 로그인 시 프론트에 쿠키로 발급한 JWT를 헤더로 옮기기 위한 컨트롤러")
    @GetMapping("/cookie-to-header")
    public ResponseEntity<Void> getJwt(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 JWT 추출
        String jwt = extractJwtFromCookie(request);

        if (jwt != null) {
            String accessToken = "Bearer " + jwt;

            // Authorization 쿠키 제거
            Cookie cookie = new Cookie("Authorization", null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);

            return ResponseEntity.ok()
                    .header("Authorization", accessToken)
                    .build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    private String extractJwtFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("Authorization".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}

