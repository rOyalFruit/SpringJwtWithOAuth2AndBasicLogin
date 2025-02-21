package com.ll.backend.controller;

import com.ll.backend.jwt.AuthConstants;
import com.ll.backend.jwt.JwtUtil;
import com.ll.backend.jwt.TokenInfo;
import com.ll.backend.service.RefreshTokenService;
import com.ll.backend.util.CookieUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Tag(name = "JwtController", description = "JWT 관련 컨트롤러")
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(("/jwt"))
@RestController
public class JwtController {

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Operation(summary = "소셜 로그인 시 프론트에 쿠키로 발급한 JWT를 헤더로 옮기기 위한 메서드")
    @GetMapping("/cookie-to-header")
    public ResponseEntity<Void> getJwt(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 JWT 추출
        Cookie[] cookies = request.getCookies();
        String jwt = CookieUtil.extractCookieValue(cookies, AuthConstants.AUTHORIZATION);

        if (jwt != null) {
            String accessToken = "Bearer " + jwt;

            // Authorization 쿠키 제거
            response.addCookie(CookieUtil.createExpiredCookie(AuthConstants.AUTHORIZATION));

            return ResponseEntity.ok()
                    .header(AuthConstants.AUTHORIZATION, accessToken)
                    .build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {

        //get refresh token
        Cookie[] cookies = request.getCookies();
        String refresh = CookieUtil.extractCookieValue(cookies, AuthConstants.AUTHORIZATION);

        if (refresh == null) {

            //response status code
            return new ResponseEntity<>("refresh token null", HttpStatus.BAD_REQUEST);
        }

        //expired check
        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {

            //response status code
            return new ResponseEntity<>("refresh token expired", HttpStatus.BAD_REQUEST);
        }

        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(refresh);

        if (!AuthConstants.REFRESH_TOKEN.equals(category)) {
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        //DB에 저장되어 있는지 확인
        if (!refreshTokenService.existsByRefreshToken(refresh)) {
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        String username = jwtUtil.getUsername(refresh);
        String role = jwtUtil.getRole(refresh);

        //make new JWT
        String newAccessToken = jwtUtil.createToken(TokenInfo.accessToken(username, role).build());
        String newRefreshToken = jwtUtil.createToken(TokenInfo.refreshToken(username, role).build());

        //Refresh 토큰 저장 DB에 기존의 Refresh 토큰 삭제 후 새 Refresh 토큰 저장
        refreshTokenService.deleteRefreshToken(refresh);
        refreshTokenService.saveRefreshToken(username, newRefreshToken);

        //response
        response.setHeader("Authorization", "Bearer " + newAccessToken);
        response.addCookie(CookieUtil.createAuthCookie(AuthConstants.REFRESH_TOKEN, newRefreshToken));

        return new ResponseEntity<>(HttpStatus.OK);
    }
}

