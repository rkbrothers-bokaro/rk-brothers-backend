package com.boltblazers.rkbrothers.core.notification;

/**
 * Abstraction over outbound notifications. The dummy implementation just
 * logs and records to notification_log; production modules can swap in a
 * real provider-backed implementation without changing callers.
 */
public interface NotificationService {

    void sendEmail(String to, String subject, String body);

    void sendWhatsApp(String phone, String message);
}
