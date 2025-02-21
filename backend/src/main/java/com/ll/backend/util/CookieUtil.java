package com.ll.backend.util;

import com.ll.backend.jwt.AuthConstants;
import jakarta.servlet.http.Cookie;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CookieUtil {

    public static Cookie createAuthCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(AuthConstants.COOKIE_MAX_AGE);
        cookie.setPath(AuthConstants.COOKIE_PATH);
        cookie.setHttpOnly(true);
        return cookie;
    }

    public static Cookie createExpiredCookie(String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setMaxAge(0);
        cookie.setPath(AuthConstants.COOKIE_PATH);
        return cookie;
    }

    public static String extractCookieValue(Cookie[] cookies, String name) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
