package com.ll.backend.domain.verification.controller;

import com.ll.backend.domain.verification.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("/verification")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    /**
     * SSE 연결 엔드포인트
     * 클라이언트가 전화번호 인증 상태를 실시간으로 받기 위한 연결 설정
     * @param phoneNumber 인증할 전화번호
     * @return SseEmitter 객체
     */
    @GetMapping(value = "/subscribe/{phoneNumber}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToVerification(@PathVariable String phoneNumber) {
        log.info("SSE 연결 요청: {}", phoneNumber);

        // SSE 연결 생성 및 반환
        SseEmitter emitter = emailVerificationService.createVerificationConnection(phoneNumber);

        // 연결 생성 실패 시 빈 SseEmitter 반환 (예외 방지)
        if (emitter == null) {
            return new SseEmitter(0L); // 즉시 완료되는 빈 이미터
        }

        return emitter;
    }
}