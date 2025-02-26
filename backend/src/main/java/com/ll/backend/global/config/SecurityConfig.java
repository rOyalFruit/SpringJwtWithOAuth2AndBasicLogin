package com.ll.backend.global.config;

import com.ll.backend.domain.auth.service.AccessTokenService;
import com.ll.backend.global.security.handler.CustomAccessDeniedHandler;
import com.ll.backend.global.security.handler.CustomAuthenticationEntryPoint;
import com.ll.backend.global.security.filter.CustomLogoutFilter;
import com.ll.backend.global.security.filter.JwtFilter;
import com.ll.backend.global.jwt.JwtUtil;
import com.ll.backend.global.security.filter.LoginFilter;
import com.ll.backend.global.security.handler.CustomSuccessHandler;
import com.ll.backend.global.security.service.CustomOAuth2UserService;
import com.ll.backend.domain.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.*;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Collections;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // 필요한 의존성 주입
    private final AuthenticationConfiguration authenticationConfiguration;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;
    private final JwtUtil jwtUtil;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    // 비밀번호 인코더 빈 등록
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 인증 매니저 빈 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // 보안 필터 체인 구성
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(this::configureCors) // CORS 설정
                .csrf(AbstractHttpConfigurer::disable) // CSRF 보호 비활성화
                .formLogin(AbstractHttpConfigurer::disable) // 폼 로그인 비활성화
                .httpBasic(AbstractHttpConfigurer::disable) // HTTP 기본 인증 비활성화
                .addFilterAfter(new JwtFilter(jwtUtil), OAuth2LoginAuthenticationFilter.class) // JWT 필터 추가
                .addFilterAt(createLoginFilter(), UsernamePasswordAuthenticationFilter.class) // 로그인 필터 추가
                .addFilterBefore(new CustomLogoutFilter(jwtUtil, accessTokenService, refreshTokenService), LogoutFilter.class) // 로그아웃 필터 추가
                .exceptionHandling(this::configureExceptionHandling) // 예외 처리 설정
                .oauth2Login(this::configureOAuth2Login) // OAuth2 로그인 설정
                .authorizeHttpRequests(this::configureAuthorization) // 요청 권한 설정
                .headers(this::configureHeaders) // 헤더 설정
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // 세션 관리 설정

        return http.build();
    }

    // CORS 설정
    private void configureCors(CorsConfigurer<HttpSecurity> corsCustomizer) {
        corsCustomizer.configurationSource(request -> {
            CorsConfiguration configuration = new CorsConfiguration();
            configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000")); // 허용할 출처
            configuration.setAllowedMethods(Collections.singletonList("*")); // 모든 HTTP 메서드 허용
            configuration.setAllowCredentials(true); // 인증 정보 허용
            configuration.setAllowedHeaders(Collections.singletonList("*")); // 모든 헤더 허용
            configuration.setMaxAge(3600L); // pre-flight 요청 결과 캐시 시간
            configuration.setExposedHeaders(Collections.singletonList("Authorization")); // 클라이언트에서 접근 가능한 헤더
            return configuration;
        });
    }

    // 로그인 필터 생성
    private LoginFilter createLoginFilter() throws Exception {
        return new LoginFilter(
                authenticationManager(authenticationConfiguration),
                jwtUtil,
                accessTokenService,
                refreshTokenService);
    }

    // 예외 처리 설정
    private void configureExceptionHandling(ExceptionHandlingConfigurer<HttpSecurity> handling) {
        handling
                .authenticationEntryPoint(customAuthenticationEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler);
    }

    // OAuth2 로그인 설정
    private void configureOAuth2Login(OAuth2LoginConfigurer<HttpSecurity> oauth2) {
        oauth2
                .userInfoEndpoint(userInfoEndpointConfig ->
                        userInfoEndpointConfig.userService(customOAuth2UserService))
                .successHandler(customSuccessHandler);
    }

    // 요청 권한 설정
    private void configureAuthorization(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth
                .requestMatchers("/", "/login", "/join/**", "/jwt/**").permitAll() // 모든 사용자에게 허용
                .requestMatchers("/h2-console/**", "/swagger-ui/**", "/v3/api-docs/**", "/qr/**", "/receive-emails").permitAll() // 개발 도구 접근 허용
                .requestMatchers("/admin/**").hasRole("ADMIN") // 관리자 권한 필요
                .anyRequest().authenticated(); // 그 외 모든 요청은 인증 필요
    }

    // 헤더 설정
    private void configureHeaders(HeadersConfigurer<HttpSecurity> headers) {
        headers.addHeaderWriter(new XFrameOptionsHeaderWriter(
                XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)); // X-Frame-Options 헤더 설정
    }
}
