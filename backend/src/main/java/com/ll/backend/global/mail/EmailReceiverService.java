//package com.ll.backend.global.mail;
//
//import jakarta.mail.*;
//import jakarta.mail.internet.MimeMultipart;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.util.Properties;
//
//@Service
//public class EmailReceiverService {
//
//    @Value("${spring.mail.username}")
//    private String username;
//
//    @Value("${spring.mail.password}")
//    private String password;
//
//    public void receiveEmails() {
//        Properties properties = new Properties();
//        properties.put("mail.store.protocol", "imaps");
//        properties.put("mail.imaps.host", "imap.gmail.com");
//        properties.put("mail.imaps.port", "993");
//        properties.put("mail.imaps.ssl.enable", "true");
//
//        try {
//            Session session = Session.getInstance(properties);
//            Store store = session.getStore("imaps");
//            store.connect(username, password);
//            System.out.println(store);
//
//            Folder inbox = store.getFolder("INBOX");
//            inbox.open(Folder.READ_ONLY);
//
//            int messageCount = inbox.getMessageCount();
//            int startIndex = Math.max(1, messageCount - 9); // 최근 10개 메일의 시작 인덱스
//
//            Message[] messages = inbox.getMessages(startIndex, messageCount);
//            for (Message message : messages) {
//                System.out.println("제목: " + message.getSubject());
//                System.out.println("보낸 사람: " + message.getFrom()[0]);
//                System.out.println("날짜: " + message.getSentDate());
//                System.out.println("내용: " + getTextFromMessage(message));
//                System.out.println("--------------------");
//            }
//
//            inbox.close(false);
//            store.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private String getTextFromMessage(Message message) throws Exception {
//        if (message.isMimeType("text/plain")) {
//            return message.getContent().toString();
//        } else if (message.isMimeType("multipart/*")) {
//            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
//            return getTextFromMimeMultipart(mimeMultipart);
//        }
//        return "";
//    }
//
//    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws Exception {
//        StringBuilder result = new StringBuilder();
//        int count = mimeMultipart.getCount();
//        for (int i = 0; i < count; i++) {
//            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
//            if (bodyPart.isMimeType("text/plain")) {
//                result.append(bodyPart.getContent());
//                break; // 일반 텍스트를 찾았으면 중단
//            } else if (bodyPart.isMimeType("text/html")) {
//                String html = (String) bodyPart.getContent();
//                result.append(org.jsoup.Jsoup.parse(html).text());
//            } else if (bodyPart.getContent() instanceof MimeMultipart) {
//                result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
//            }
//        }
//        return result.toString();
//    }
//}
