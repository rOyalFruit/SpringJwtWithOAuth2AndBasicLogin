package com.ll.backend.global.jwt;

public class AuthConstants {
    // JWT Related Constants
    public static final String ACCESS_TOKEN = "accessToken";
    public static final String REFRESH_TOKEN = "refreshToken";
    public static final String AUTHORIZATION = "Authorization";

    // Token Expiration Times (in milliseconds)
    public static final long ACCESS_TOKEN_EXPIRATION = 60 * 10 * 1000L; // 10 minutes
    public static final long REFRESH_TOKEN_EXPIRATION = 60 * 60 * 24 * 1000L; // 24 hours

    // Cookie Settings
    public static final int COOKIE_MAX_AGE = (int)(REFRESH_TOKEN_EXPIRATION / 1000); // 24 hours in seconds
    public static final String COOKIE_PATH = "/";

    // URLs
    public static final String AUTH_SUCCESS_REDIRECT_URL = "http://localhost:3000/auth-success";
}