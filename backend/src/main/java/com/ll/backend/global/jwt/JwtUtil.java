package com.ll.backend.global.jwt;

import com.ll.backend.domain.auth.service.RefreshTokenService;
import com.ll.backend.global.exception.auth.token.ExpiredTokenException;
import com.ll.backend.global.exception.auth.token.InvalidTokenException;
import com.ll.backend.global.exception.auth.token.TokenNotFoundException;
import com.ll.backend.global.util.CookieUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final RefreshTokenService refreshTokenService;

    public JwtUtil(@Value("${spring.jwt.secret}") String secret, RefreshTokenService refreshTokenService) {

        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.refreshTokenService = refreshTokenService;
    }

    public String getUsername(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("username", String.class);
    }

    public String getRole(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    public Boolean isExpired(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }

    public String getCategory(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("category", String.class);
    }

    public String createToken(TokenInfo tokenInfo) {
        return Jwts.builder()
                .setSubject(tokenInfo.username())
                .claim("role", tokenInfo.role())
                .claim("category", tokenInfo.category())
                .setExpiration(new Date(System.currentTimeMillis() + tokenInfo.expirationTime()))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractAccessToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new TokenNotFoundException();
        }
        return authorizationHeader.substring(7);
    }

    public String extractRefreshToken(Cookie[] cookies) {
        String refreshToken = CookieUtil.extractCookieValue(cookies, AuthConstants.REFRESH_TOKEN);

        if (refreshToken == null) {
            throw new TokenNotFoundException();
        }

        return refreshToken;
    }

    public void validateAccessToken(String token) {
        if (isExpired(token)) {
            throw new ExpiredTokenException();
        }

        String category = getCategory(token);
        if (!category.equals(AuthConstants.ACCESS_TOKEN)) {
            throw new InvalidTokenException();
        }
    }

    public void validateRefreshToken(String refreshToken) {
        try {
            if (isExpired(refreshToken)) {
                throw new ExpiredTokenException();
            }
        } catch (ExpiredJwtException e) {
            throw new ExpiredTokenException();
        }

        String category = getCategory(refreshToken);
        if (!category.equals(AuthConstants.REFRESH_TOKEN)) {
            throw new InvalidTokenException();
        }

        if (!refreshTokenService.existsByRefreshToken(refreshToken)) {
            throw new InvalidTokenException();
        }
    }
}
