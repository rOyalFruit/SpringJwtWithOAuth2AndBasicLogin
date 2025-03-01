package com.ll.backend.domain.verification.controller;

import com.ll.backend.domain.member.dto.PhoneVerificationRequestDto;
import com.ll.backend.domain.verification.service.PhoneVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/verification")
@RequiredArgsConstructor
public class PhoneVerificationController {

    private final PhoneVerificationService phoneVerificationService;

    @PostMapping(path = "/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] generateQRCode(@RequestBody PhoneVerificationRequestDto request,
                                 @RequestParam(defaultValue = "250") int width,
                                 @RequestParam(defaultValue = "250") int height) throws Exception {

        PhoneVerificationRequestDto requestDto = new PhoneVerificationRequestDto(
                request.phone(),
                LocalDateTime.now()
        );

        return phoneVerificationService.processGenerateQrRequest(requestDto, width, height);
    }
}
