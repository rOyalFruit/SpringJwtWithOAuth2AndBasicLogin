package com.ll.backend.global.mail;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmailController {

    private final EmailReceiverService emailReceiverService;

    public EmailController(EmailReceiverService emailReceiverService) {
        this.emailReceiverService = emailReceiverService;
    }

    @GetMapping("/receive-emails")
    public String receiveEmails() {
        emailReceiverService.receiveEmails();
        return "이메일 수신 완료";
    }
}
