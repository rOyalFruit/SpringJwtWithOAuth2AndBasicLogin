package com.ll.backend.global.config;

import com.ll.backend.domain.verification.service.EmailVerificationService;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.search.AndTerm;
import jakarta.mail.search.ComparisonTerm;
import jakarta.mail.search.FlagTerm;
import jakarta.mail.search.ReceivedDateTerm;
import jakarta.mail.search.SearchTerm;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.mail.ImapIdleChannelAdapter;
import org.springframework.integration.mail.ImapMailReceiver;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@Configuration
@EnableIntegration
@RequiredArgsConstructor
public class EmailIntegrationConfig {

    private static final Logger logger = LoggerFactory.getLogger(EmailIntegrationConfig.class);
    private final ApplicationContext applicationContext;
    private final Date applicationStartTime = new Date();
    private final EmailVerificationService emailVerificationService;

    // 허용된 도메인 목록
    private static final List<String> ALLOWED_DOMAINS = Arrays.asList(
            "vmms.nate.com",
            "ktfmms.magicn.com",
            "mmsmail.uplus.co.kr"
    );

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Bean
    public MessageChannel emailInChannel() {
        return new DirectChannel();
    }

    @Bean
    public ImapMailReceiver imapMailReceiver() {
        ImapMailReceiver receiver = new ImapMailReceiver(createImapUrl());
        receiver.setJavaMailProperties(mailProperties());
        receiver.setShouldMarkMessagesAsRead(true);
        receiver.setShouldDeleteMessages(false);
        receiver.setSimpleContent(true);
        receiver.setSearchTermStrategy(this::generateSearchTerm);
        receiver.setBeanFactory(applicationContext);
        return receiver;
    }

    @Bean
    public ImapIdleChannelAdapter imapIdleChannelAdapter() {
        ImapIdleChannelAdapter adapter = new ImapIdleChannelAdapter(imapMailReceiver());
        adapter.setAutoStartup(true);
        adapter.setOutputChannelName("emailInChannel");
        return adapter;
    }

    @ServiceActivator(inputChannel = "emailInChannel")
    public void handleMessage(Message<?> message) {
        if (!(message.getPayload() instanceof MimeMessage email)) {
            return;
        }

        try {
            Date receivedDate = email.getReceivedDate();
            String subject = email.getSubject();
            String content = extractContentSafely(email);
            String sender = extractSenderEmail(email);

            if (isFromAllowedDomain(sender)) {
                logger.info("\n===== 새 메일 수신 =====\n시간: {}\n발신자: {}\n제목: {}\n메일 내용:\n{}",
                        receivedDate, sender, subject, content);
                emailVerificationService.processEmailVerification(sender);
            }
        } catch (MessagingException e) {
            logger.error("메일 처리 중 오류 발생", e);
        }
    }

    private boolean isFromAllowedDomain(String emailAddress) {
        if (emailAddress == null || emailAddress.isEmpty()) {
            return false;
        }

        // @ 기호 이후의 도메인 부분 추출
        int atIndex = emailAddress.lastIndexOf('@');
        if (atIndex == -1 || atIndex == emailAddress.length() - 1) {
            return false;
        }

        String domain = emailAddress.substring(atIndex + 1).toLowerCase();
        return ALLOWED_DOMAINS.contains(domain);
    }

    private String extractSenderEmail(MimeMessage message) throws MessagingException {
        Address[] fromAddresses = message.getFrom();
        if (fromAddresses != null && fromAddresses.length > 0) {
            Address address = fromAddresses[0];
            if (address instanceof InternetAddress) {
                return ((InternetAddress) address).getAddress();
            } else {
                return address.toString();
            }
        }
        return "알 수 없는 발신자";
    }

    private String extractContentSafely(MimeMessage message) {
        try {
            Object content = message.getContent();
            if (content instanceof String) {
                return (String) content;
            } else if (content instanceof Multipart) {
                return extractTextFromMultipart((Multipart) content);
            }
        } catch (Exception e) {
            logger.warn("메일 내용 추출 중 오류 발생", e);
        }
        return "[메일 내용을 추출할 수 없습니다]";
    }

    private String extractTextFromMultipart(Multipart multipart) throws MessagingException, IOException {
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                return (String) bodyPart.getContent();
            }
        }
        return "[멀티파트 메일에서 텍스트 내용을 찾을 수 없습니다]";
    }

    private SearchTerm generateSearchTerm(Flags supportedFlags, Folder folder) {
        FlagTerm unseenFlagTerm = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
        ReceivedDateTerm receivedDateTerm = new ReceivedDateTerm(ComparisonTerm.GE, applicationStartTime);
        return new AndTerm(unseenFlagTerm, receivedDateTerm);
    }

    private Properties mailProperties() {
        Properties properties = new Properties();
        properties.setProperty("mail.store.protocol", "imaps");
        properties.setProperty("mail.imaps.ssl.enable", "true");
        properties.setProperty("mail.imaps.ssl.trust", "*");
        properties.setProperty("mail.imaps.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.imaps.socketFactory.fallback", "false");
        properties.setProperty("mail.imaps.port", "993");
        properties.setProperty("mail.imaps.connectiontimeout", "60000");
        properties.setProperty("mail.imaps.timeout", "60000");
        properties.setProperty("mail.imap.folderopen.mode", "rw");
        return properties;
    }

    private String createImapUrl() {
        String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8);
        String encodedPassword = URLEncoder.encode(password, StandardCharsets.UTF_8);
        return String.format("imaps://%s:%s@imap.gmail.com:993/INBOX", encodedUsername, encodedPassword);
    }
}
