package com.ll.backend.config;

import com.ll.backend.jwt.CustomLogoutFilter;
import com.ll.backend.jwt.JwtFilter;
import com.ll.backend.jwt.JwtUtil;
import com.ll.backend.jwt.LoginFilter;
import com.ll.backend.oauth2.CustomSuccessHandler;
import com.ll.backend.service.CustomOAuth2UserService;
import com.ll.backend.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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

    private final AuthenticationConfiguration authenticationConfiguration;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors((corsCustomizer -> corsCustomizer.configurationSource(request -> {

                    CorsConfiguration configuration = new CorsConfiguration();

                    // 허용할 출처 설정 (React 앱의 주소)
                    configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
                    // 모든 HTTP 메서드 허용
                    configuration.setAllowedMethods(Collections.singletonList("*"));
                    // 인증 정보 (쿠키 등) 허용
                    configuration.setAllowCredentials(true);
                    // 모든 헤더 허용
                    configuration.setAllowedHeaders(Collections.singletonList("*"));
                    // pre-flight 요청 결과를 1시간 동안 캐시(pre-flight: 실제 요청 전에 브라우저가 보내는 OPTIONS 요청.)
                    configuration.setMaxAge(3600L);
                    // 클라이언트에서 접근 가능한 헤더 설정
                    configuration.setExposedHeaders(Collections.singletonList("Authorization"));

                    return configuration;
                })))
                // CSRF(Cross-Site Request Forgery) 보호 기능 비활성.
                .csrf(AbstractHttpConfigurer::disable)
                // 폼 로그인 비활성화: JWT를 사용하므로 폼 로그인은 사용하지 않음
                .formLogin(AbstractHttpConfigurer::disable)
                // HTTP Basic 인증 비활성화: JWT를 사용하므로 Basic 인증은 사용하지 않음
                .httpBasic(AbstractHttpConfigurer::disable)
                // JwtFilter를 추가하여 JWT 인증 처리
                .addFilterAfter(new JwtFilter(jwtUtil), OAuth2LoginAuthenticationFilter.class)
                // LoginFilter를 추가하여 로그인 처리
                .addFilterAt(new LoginFilter(
                                authenticationManager(authenticationConfiguration),
                                jwtUtil,
                                refreshTokenService),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new CustomLogoutFilter(jwtUtil, refreshTokenService), LogoutFilter.class)
                // OAuth 2.0 로그인 시 사용되는 서비스 설정
                // 1. userInfoEndpoint(): OAuth 2.0 공급자로부터 사용자 정보를 가져오는 엔드포인트를 구성.
                // 2. userService(customOAuth2UserService):
                //    - OAuth 2.0 공급자로부터 받은 사용자 정보를 처리할 커스텀 서비스를 지정.
                //    - CustomOAuth2UserService는 OAuth2UserService 인터페이스를 구현한 클래스.
                //    - 이 서비스에서 소셜 로그인 후의 사용자 정보 처리 로직을 구현
                .oauth2Login((oauth2) -> oauth2
                        .userInfoEndpoint((userInfoEndpointConfig) -> userInfoEndpointConfig
                                .userService(customOAuth2UserService))
                        .successHandler(customSuccessHandler))
                // 경로별 인가 작업
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/", "/login", "/join", "/jwt/**").permitAll()
                        .requestMatchers("/h2-console/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                // X-Frame-Options 헤더 설정: H2 콘솔 접근을 위해 SAMEORIGIN으로 설정
                .headers(headers -> headers
                        .addHeaderWriter(new XFrameOptionsHeaderWriter(
                                XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)
                        )
                )
                // 세션 관리 설정: STATELESS로 설정
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }
}
