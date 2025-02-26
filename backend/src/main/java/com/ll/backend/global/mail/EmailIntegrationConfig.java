package com.ll.backend.global.mail;

import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.search.FlagTerm;
import jakarta.mail.search.SearchTerm;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.mail.ImapIdleChannelAdapter;
import org.springframework.integration.mail.ImapMailReceiver;
import org.springframework.integration.mail.MailReceivingMessageSource;
import org.springframework.integration.mail.SearchTermStrategy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Properties;

@Configuration
@EnableIntegration
@RequiredArgsConstructor
public class EmailIntegrationConfig {

    private static final Logger logger = LoggerFactory.getLogger(EmailIntegrationConfig.class);
    private final ApplicationContext applicationContext;

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

        // 새 메일만 가져오도록 SearchTerm 설정
        receiver.setSearchTermStrategy(new SearchTermStrategy() {
            @Override
            public SearchTerm generateSearchTerm(Flags supportedFlags, Folder folder) {
                logger.info("Searching for unread messages...");
                return new FlagTerm(new Flags(Flags.Flag.SEEN), false);
            }
        });

        // BeanFactory 설정 추가
        receiver.setBeanFactory(applicationContext);

        return receiver;
    }

    @Bean
    public ImapIdleChannelAdapter imapIdleChannelAdapter() {
        ImapIdleChannelAdapter adapter = new ImapIdleChannelAdapter(imapMailReceiver());
        adapter.setAutoStartup(true);
        adapter.setOutputChannel(emailInChannel());

        // 중요: 이벤트 핸들러 등록
        adapter.afterPropertiesSet();

        return adapter;
    }

    // 폴링 방식으로도 메일 확인 (백업 메커니즘)
    @Bean
    public MessageSource<Object> pollingMailMessageSource() {
        ImapMailReceiver receiver = new ImapMailReceiver(createImapUrl());
        receiver.setJavaMailProperties(mailProperties());
        receiver.setShouldMarkMessagesAsRead(true);
        receiver.setShouldDeleteMessages(false);
        receiver.setSearchTermStrategy(new SearchTermStrategy() {
            @Override
            public SearchTerm generateSearchTerm(Flags supportedFlags, Folder folder) {
                logger.info("Polling for unread messages...");
                return new FlagTerm(new Flags(Flags.Flag.SEEN), false);
            }
        });
        receiver.setBeanFactory(applicationContext);

        return new MailReceivingMessageSource(receiver);
    }

    @Bean
    @InboundChannelAdapter(
            value = "emailInChannel",
            poller = @Poller(fixedDelay = "30000") // 30초마다 폴링
    )
    public MessageSource<Object> mailPollingSource() {
        return pollingMailMessageSource();
    }

    @ServiceActivator(inputChannel = "emailInChannel")
    public void handleMessage(Message<?> message) {
        try {
            logger.info("Message received of type: {}", message.getPayload().getClass().getName());

            if (message.getPayload() instanceof MimeMessage) {
                MimeMessage email = (MimeMessage) message.getPayload();

                // 메일 제목과 수신 시간 로깅
                Date receivedDate = email.getReceivedDate();
                String subject = email.getSubject();

                logger.info("메일 수신 시간: {}, 제목: {}", receivedDate, subject);
                System.out.println("새 메일 도착: " + subject);
                System.out.println("수신 시간: " + receivedDate);

                // 여기에 추가적인 메일 처리 로직 구현 가능
            } else {
                logger.warn("Received message is not a MimeMessage: {}", message.getPayload());
                System.out.println("메시지 타입이 MimeMessage가 아님: " + message.getPayload().getClass().getName());
            }
        } catch (MessagingException e) {
            logger.error("메일 처리 중 오류 발생", e);
            System.out.println("메일 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("예상치 못한 오류 발생", e);
            System.out.println("예상치 못한 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Properties mailProperties() {
        Properties properties = new Properties();
        properties.setProperty("mail.store.protocol", "imaps");
        properties.setProperty("mail.imaps.ssl.enable", "true");
        properties.setProperty("mail.imaps.ssl.trust", "*");
        properties.setProperty("mail.imaps.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.imaps.socketFactory.fallback", "false");
        properties.setProperty("mail.imaps.port", "993");
//        properties.setProperty("mail.debug", "true"); // 디버그 모드 활성화
        properties.setProperty("mail.imaps.connectiontimeout", "15000");
        properties.setProperty("mail.imaps.timeout", "10000");

        // IDLE 명령 지원 설정
        properties.setProperty("mail.imap.starttls.enable", "true");
        properties.setProperty("mail.imap.auth", "true");
        properties.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.imap.socketFactory.fallback", "false");
        properties.setProperty("mail.imap.port", "993");

        return properties;
    }

    private String createImapUrl() {
        try {
            String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8.toString());
            String encodedPassword = URLEncoder.encode(password, StandardCharsets.UTF_8.toString());
            return String.format("imaps://%s:%s@imap.gmail.com:993/INBOX", encodedUsername, encodedPassword);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URL 인코딩 실패", e);
        }
    }
}