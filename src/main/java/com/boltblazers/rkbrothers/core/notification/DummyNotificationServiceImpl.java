package com.boltblazers.rkbrothers.core.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Active outside prod only. JavaMailNotificationServiceImpl is @Profile("prod");
 * without this restriction both beans would be active simultaneously in
 * prod, and Spring would fail to start with a NoUniqueBeanDefinitionException
 * wherever NotificationService is injected.
 */
@Slf4j
@Service
@Profile("!prod")
@RequiredArgsConstructor
public class DummyNotificationServiceImpl implements NotificationService {

    private final NotificationLogRepository notificationLogRepository;

    @Override
    public void sendEmail(String to, String subject, String body) {
        log.info("EMAIL TO: {} | SUBJECT: {} | BODY: {}", to, subject, body);

        NotificationLog entry = NotificationLog.builder()
                .type(NotificationType.EMAIL)
                .recipient(to)
                .subject(subject)
                .message(body)
                .status(NotificationStatus.STUB)
                .createdAt(LocalDateTime.now())
                .build();
        notificationLogRepository.save(entry);
    }

    @Override
    public void sendWhatsApp(String phone, String message) {
        log.info("WHATSAPP STUB — not configured");
    }
}
