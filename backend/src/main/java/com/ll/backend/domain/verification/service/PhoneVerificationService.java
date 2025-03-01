package com.ll.backend.domain.verification.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.ll.backend.domain.member.dto.PhoneVerificationRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PhoneVerificationService {

    @Value("${spring.mail.username}")
    private String mailAddress;

    private final PhoneVerificationService self;

    @Autowired
    public PhoneVerificationService(@Lazy PhoneVerificationService self) {
        this.self = self;
    }

    private static final String CACHE_NAME = "phoneVerification";

    public byte[] processGenerateQrRequest(PhoneVerificationRequestDto requestDto, int width, int height) throws WriterException, IOException {

        String qrBody = createMessageBody(requestDto);

        self.storeVerificationInfo(requestDto);

        return generateQRCode(qrBody, width, height); // QR 코드 생성
    }

    public String createMessageBody(PhoneVerificationRequestDto requestDto) {
        String bodyContent = "[인증코드 요청]"
                             + "\n시간: " + requestDto.timestamp()
                             + "\n요청 번호: " + requestDto.phone();

        return Base64.getEncoder().encodeToString(bodyContent.getBytes(StandardCharsets.UTF_8));
    }

    public byte[] generateQRCode(String body, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());

        String plainContent = "sms:" + mailAddress + "?body=" + body;

        BitMatrix bitMatrix = qrCodeWriter.encode(plainContent, BarcodeFormat.QR_CODE, width, height, hints);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

        return pngOutputStream.toByteArray();
    }

    @CachePut(value = CACHE_NAME, key = "#requestDto.phone()")
    public PhoneVerificationRequestDto storeVerificationInfo(PhoneVerificationRequestDto requestDto) {
        log.info("Storing verification info for phone: {}", requestDto.phone());
        return requestDto;
    }

    @CacheEvict(value = CACHE_NAME, key = "#requestDto.phone()")
    public void removeVerificationInfo(PhoneVerificationRequestDto requestDto) {
        // @CacheEvict 어노테이션이 캐시 삭제를 처리
    }
}