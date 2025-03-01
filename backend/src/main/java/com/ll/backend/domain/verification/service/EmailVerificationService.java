package com.ll.backend.domain.verification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final RedisTemplate<String, String> redisTemplate;

    // 전화번호와 SSE 이미터를 매핑하는 저장소
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    // SSE 연결 타임아웃 (5분)
    private static final long SSE_TIMEOUT = 5 * 60 * 1000L;

    // Redis 키 접두사
    private static final String PHONE_VERIFICATION_PREFIX = "phoneVerification::";

    // 전화번호 패턴 (숫자로만 구성된 11자리)
    private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("^\\d{11}$");

    // JSON 형식의 전화번호 추출 패턴
    private static final Pattern PHONE_JSON_PATTERN = Pattern.compile("\"phone\":\"([^\"]+)\"");

    /**
     * QR 코드 스캔 후 이메일 수신 시 호출되어 인증 처리
     * @param senderEmail 발신자 이메일 주소
     */
    public void processEmailVerification(String senderEmail) {
        // 이메일에서 @ 앞부분 추출
        String emailId = extractEmailId(senderEmail);

        // Redis에서 해당 키로 저장된 정보 확인
        String redisKey = PHONE_VERIFICATION_PREFIX + emailId;
        String phoneData = redisTemplate.opsForValue().get(redisKey);

        if (phoneData != null) {
            // JSON 형식이면 파싱하여 전화번호 추출
            String phoneNumber = extractPhoneNumber(phoneData);
            log.info("인증 성공: 전화번호 = {}", phoneNumber);

            // SSE를 통해 클라이언트에게 알림
            sendVerificationCompleteEvent(phoneNumber);

            // 인증에 사용된 임시 데이터 삭제
            redisTemplate.delete(redisKey);
        } else {
            log.warn("인증 실패: 전화번호 [{}] 에 대한 정보 없음", emailId);
        }
    }

    /**
     * Redis에서 가져온 데이터에서 전화번호 추출
     * @param phoneData Redis에서 가져온 데이터
     * @return 추출된 전화번호
     */
    private String extractPhoneNumber(String phoneData) {
        // JSON 형식인 경우 파싱
        if (phoneData.startsWith("{")) {
            try {
                Matcher matcher = PHONE_JSON_PATTERN.matcher(phoneData);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            } catch (Exception e) {
                log.error("전화번호 추출 중 오류", e);
            }
        }
        return phoneData; // 파싱 실패 시 원본 반환
    }

    /**
     * 전화번호 인증 요청 시 SSE 연결 생성
     * @param phoneNumber 인증할 전화번호
     * @return SseEmitter 객체
     */
    public SseEmitter createVerificationConnection(String phoneNumber) {
        try {
            // 기존 연결이 있으면 제거
            removeEmitter(phoneNumber);

            // 새 SSE 이미터 생성 (타임아웃 설정)
            SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

            // 완료, 타임아웃, 에러 시 이미터 제거
            emitter.onCompletion(() -> removeEmitter(phoneNumber));
            emitter.onTimeout(() -> removeEmitter(phoneNumber));
            emitter.onError(e -> {
                log.error("SSE 에러 발생: {}", e.getMessage());
                removeEmitter(phoneNumber);
            });

            // 연결 성공 이벤트 전송
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("QR 코드를 스캔하여 인증을 완료해주세요."));

            // 이미터 저장
            emitters.put(phoneNumber, emitter);
            log.info("전화번호 {} 에 대한 SSE 연결 생성됨", phoneNumber);

            return emitter;
        } catch (IOException e) {
            log.error("SSE 연결 생성 중 오류: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("SSE 연결 처리 중 예외 발생: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 클라이언트에게 인증 완료 이벤트 전송
     * @param phoneNumber 인증된 전화번호
     */
    private void sendVerificationCompleteEvent(String phoneNumber) {
        SseEmitter emitter = emitters.get(phoneNumber);

        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("verificationComplete")
                        .data("인증이 완료되었습니다."));

                // 이벤트 전송 후 연결 종료
                emitter.complete();
            } catch (IOException e) {
                log.error("인증 완료 이벤트 전송 실패: {}", e.getMessage());
                removeEmitter(phoneNumber);
            }
        } else {
            log.warn("전화번호 {} 에 대한 활성 SSE 연결 없음", phoneNumber);
        }
    }

    /**
     * 이메일 주소에서 @ 앞부분 추출하고, 전화번호 형식이면 하이픈 추가
     * @param email 이메일 주소
     * @return 포맷팅된 문자열
     */
    private String extractEmailId(String email) {
        if (email != null && email.contains("@")) {
            String emailId = email.split("@")[0];

            // 전화번호 형식인지 확인 (11자리 숫자)
            if (PHONE_NUMBER_PATTERN.matcher(emailId).matches()) {
                // 전화번호 형식이면 하이픈 추가 (010-7136-7472 형식)
                return formatPhoneNumber(emailId);
            }
            return emailId;
        }
        return email;
    }

    /**
     * 전화번호에 하이픈 추가 (01011111111 -> 010-1111-1111)
     * @param phoneNumber 하이픈 없는 전화번호
     * @return 하이픈이 추가된 전화번호
     */
    private String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() != 11) {
            return phoneNumber;
        }

        StringBuilder formatted = new StringBuilder();
        formatted.append(phoneNumber, 0, 3);
        formatted.append('-');
        formatted.append(phoneNumber, 3, 7);
        formatted.append('-');
        formatted.append(phoneNumber, 7, 11);

        return formatted.toString();
    }

    /**
     * SSE 이미터 제거
     * @param phoneNumber 전화번호
     */
    private void removeEmitter(String phoneNumber) {
        emitters.remove(phoneNumber);
        log.info("전화번호 {} 에 대한 SSE 연결 제거됨", phoneNumber);
    }
}