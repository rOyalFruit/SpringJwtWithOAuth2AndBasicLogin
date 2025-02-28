package com.ll.backend.global.mail;

import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.MessagingException;
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

    // 애플리케이션 시작 시간을 저장 - 이 시간 이후의 메일만 처리
    private final Date applicationStartTime = new Date();

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
        // URL에 폴더 모드를 지정하여 READ_WRITE로 설정
        ImapMailReceiver receiver = new ImapMailReceiver(createImapUrl());

        // JavaMail 속성 설정
        Properties properties = mailProperties();
        receiver.setJavaMailProperties(properties);

        receiver.setShouldMarkMessagesAsRead(true);
        receiver.setShouldDeleteMessages(false);

        // 애플리케이션 시작 이후에 수신된 읽지 않은 메일만 가져오도록 SearchTerm 설정
        receiver.setSearchTermStrategy(new SearchTermStrategy() {
            @Override
            public SearchTerm generateSearchTerm(Flags supportedFlags, Folder folder) {
                logger.info("Searching for new unread messages...");

                try {
                    // 중요: 폴더 모드를 READ_WRITE로 설정
                    if (folder.getMode() != Folder.READ_WRITE) {
                        folder.close(false);
                        folder.open(Folder.READ_WRITE);
                        logger.info("Folder reopened in READ_WRITE mode");
                    }
                } catch (MessagingException e) {
                    logger.error("폴더 모드 변경 중 오류 발생", e);
                }

                // 읽지 않은 메일 필터
                FlagTerm unseenFlagTerm = new FlagTerm(new Flags(Flags.Flag.SEEN), false);

                // 애플리케이션 시작 이후 수신된 메일 필터
                ReceivedDateTerm receivedDateTerm = new ReceivedDateTerm(
                        ComparisonTerm.GE, applicationStartTime);

                // 두 조건을 AND로 결합
                return new AndTerm(unseenFlagTerm, receivedDateTerm);
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
        adapter.setOutputChannelName("emailInChannel");
        return adapter;
    }

    // 폴링 방식으로도 메일 확인 (백업 메커니즘)
    @Bean
    public MessageSource<Object> pollingMailMessageSource() {
        // URL에 폴더 모드를 지정하여 READ_WRITE로 설정
        ImapMailReceiver receiver = new ImapMailReceiver(createImapUrl());

        // JavaMail 속성 설정
        Properties properties = mailProperties();
        receiver.setJavaMailProperties(properties);

        receiver.setShouldMarkMessagesAsRead(true);
        receiver.setShouldDeleteMessages(false);

        receiver.setSearchTermStrategy(new SearchTermStrategy() {
            @Override
            public SearchTerm generateSearchTerm(Flags supportedFlags, Folder folder) {
                logger.info("Polling for new unread messages...");

                try {
                    // 중요: 폴더 모드를 READ_WRITE로 설정
                    if (folder.getMode() != Folder.READ_WRITE) {
                        folder.close(false);
                        folder.open(Folder.READ_WRITE);
                        logger.info("Folder reopened in READ_WRITE mode");
                    }
                } catch (MessagingException e) {
                    logger.error("폴더 모드 변경 중 오류 발생", e);
                }

                // 읽지 않은 메일 필터
                FlagTerm unseenFlagTerm = new FlagTerm(new Flags(Flags.Flag.SEEN), false);

                // 애플리케이션 시작 이후 수신된 메일 필터
                ReceivedDateTerm receivedDateTerm = new ReceivedDateTerm(
                        ComparisonTerm.GE, applicationStartTime);

                // 두 조건을 AND로 결합
                return new AndTerm(unseenFlagTerm, receivedDateTerm);
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
        // 타임아웃 설정 증가
        properties.setProperty("mail.imaps.connectiontimeout", "30000"); // 30초
        properties.setProperty("mail.imaps.timeout", "30000"); // 30초

        // IDLE 명령 지원 설정
        properties.setProperty("mail.imap.starttls.enable", "true");
        properties.setProperty("mail.imap.auth", "true");
        properties.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.imap.socketFactory.fallback", "false");
        properties.setProperty("mail.imap.port", "993");
        // IMAP IDLE 타임아웃 설정
        properties.setProperty("mail.imap.connectiontimeout", "30000"); // 30초
        properties.setProperty("mail.imap.timeout", "30000"); // 30초

        // 중요: 폴더를 READ_WRITE 모드로 열도록 설정
        properties.setProperty("mail.imap.folderopen.mode", "rw");
        properties.setProperty("mail.imaps.folderopen.mode", "rw");

        // IDLE 커맨드 타임아웃 설정
        properties.setProperty("mail.imap.idletimeout", "1800000"); // 30분

        // 연결 유지를 위한 설정
        properties.setProperty("mail.imap.keepalive.interval", "300000"); // 5분마다 keepalive

        // 디버그 모드 활성화 (문제 해결 시 유용)
        // properties.setProperty("mail.debug", "true");

        return properties;
    }

    private String createImapUrl() {
        String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8);
        String encodedPassword = URLEncoder.encode(password, StandardCharsets.UTF_8);
        // URL에 폴더 모드 파라미터 추가
        return "imaps://" + encodedUsername + ":" + encodedPassword + "@imap.gmail.com:993/INBOX";
    }
}