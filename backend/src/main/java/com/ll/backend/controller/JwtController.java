package com.ll.backend.controller;

import com.ll.backend.entity.RefreshEntity;
import com.ll.backend.jwt.AuthConstants;
import com.ll.backend.jwt.JwtUtil;
import com.ll.backend.repository.RefreshRepository;
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

import java.util.Date;

@RequiredArgsConstructor
@Tag(name = "JwtController", description = "JWT 관련 컨트롤러")
@SecurityRequirement(name = "bearerAuth")
@RequestMapping(("/jwt"))
@RestController
public class JwtController {

    private final JwtUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    @Operation(summary = "소셜 로그인 시 프론트에 쿠키로 발급한 JWT를 헤더로 옮기기 위한 메서드")
    @GetMapping("/cookie-to-header")
    public ResponseEntity<Void> getJwt(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 JWT 추출
        String jwt = extractJwtFromCookie(request);

        if (jwt != null) {
            String accessToken = "Bearer " + jwt;

            // Authorization 쿠키 제거
            Cookie cookie = new Cookie(AuthConstants.AUTHORIZATION, null);
            cookie.setMaxAge(0);
            cookie.setPath(AuthConstants.COOKIE_PATH);
            response.addCookie(cookie);

            return ResponseEntity.ok()
                    .header(AuthConstants.AUTHORIZATION, accessToken)
                    .build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    private String extractJwtFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(AuthConstants.AUTHORIZATION)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {

        //get refresh token
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {

            if (cookie.getName().equals("refreshToken")) {

                refresh = cookie.getValue();
            }
        }

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

        if (!category.equals("refresh")) {

            //response status code
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        //DB에 저장되어 있는지 확인
        Boolean isExist = refreshRepository.existsByRefresh(refresh);
        if (!isExist) {

            //response body
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        String username = jwtUtil.getUsername(refresh);
        String role = jwtUtil.getRole(refresh);

        //make new JWT
        String newAccessToken = jwtUtil.createJwt("access", username, role, 60 * 10 * 1000L);
        String newRefreshToken = jwtUtil.createJwt("refresh", username, role, 60 * 60 * 24 * 1000L);

        //Refresh 토큰 저장 DB에 기존의 Refresh 토큰 삭제 후 새 Refresh 토큰 저장
        refreshRepository.deleteByRefresh(refresh);
        addRefreshEntity(username, newRefreshToken, 60 * 60 * 24 * 1000L);

        //response
        response.setHeader("Authorization", "Bearer " + newAccessToken);
        response.addCookie(createCookie("refreshToken", newRefreshToken));

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        //cookie.setSecure(true);
        //cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }

    private void addRefreshEntity(String username, String refresh, Long expiredMs) {

        Date date = new Date(System.currentTimeMillis() + expiredMs);

        RefreshEntity refreshEntity = new RefreshEntity();
        refreshEntity.setUsername(username);
        refreshEntity.setRefresh(refresh);
        refreshEntity.setExpiration(date.toString());

        refreshRepository.save(refreshEntity);
    }
}

