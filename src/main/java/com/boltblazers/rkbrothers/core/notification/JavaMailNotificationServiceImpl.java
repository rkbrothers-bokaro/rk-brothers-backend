package com.boltblazers.rkbrothers.core.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Real email delivery via Jakarta Mail, active only in prod. Every failure
 * path (send exception) is caught and logged rather than thrown — a failed
 * notification must never crash the caller (e.g. the expiry scheduler).
 * notification_log is always updated with the actual outcome.
 */
@Slf4j
@Service
@Profile("prod")
@RequiredArgsConstructor
public class JavaMailNotificationServiceImpl implements NotificationService {

    private final JavaMailSender javaMailSender;
    private final NotificationLogRepository notificationLogRepository;

    @Value("${app.mail-from:}")
    private String mailFrom;

    @Override
    public void sendEmail(String to, String subject, String body) {
        NotificationLog entry = NotificationLog.builder()
                .type(NotificationType.EMAIL)
                .recipient(to)
                .subject(subject)
                .message(body)
                .status(NotificationStatus.FAILED)
                .createdAt(LocalDateTime.now())
                .build();

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            javaMailSender.send(message);

            entry.setStatus(NotificationStatus.SENT);
            entry.setSentAt(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            entry.setStatus(NotificationStatus.FAILED);
        }

        notificationLogRepository.save(entry);
    }

    @Override
    public void sendWhatsApp(String phone, String message) {
        log.info("WHATSAPP STUB — not configured");
    }
}
