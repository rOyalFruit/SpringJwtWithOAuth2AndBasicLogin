package com.ll.backend.domain.member.controller;

import com.ll.backend.domain.member.service.QRCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/qr")
public class QRCodeController {

    private final QRCodeService qrCodeService;

    @GetMapping(produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] generateQRCode(@RequestParam String text, // @RequestBody를 사용할 경우 공백을 %20으로 처리해야 함. 인코딩과정 번거로우니 @RequestParam 사용할 것.
                                 @RequestParam(defaultValue = "250") int width,
                                 @RequestParam(defaultValue = "250") int height) throws Exception {
        return qrCodeService.generateQRCode(text, width, height);
    }

}
