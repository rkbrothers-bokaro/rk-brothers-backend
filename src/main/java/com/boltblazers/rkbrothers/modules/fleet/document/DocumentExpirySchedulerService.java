package com.boltblazers.rkbrothers.modules.fleet.document;

import com.boltblazers.rkbrothers.core.notification.EmailTemplateService;
import com.boltblazers.rkbrothers.core.notification.EmailTemplateService.EmailContent;
import com.boltblazers.rkbrothers.core.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentExpirySchedulerService {

    private static final List<Integer> REMINDER_WINDOWS_DAYS = List.of(30, 15, 7);

    private final VehicleDocumentRepository vehicleDocumentRepository;
    private final NotificationService notificationService;
    private final EmailTemplateService emailTemplateService;

    @Value("${app.admin-email:}")
    private String adminEmail;

    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public int checkAndSendReminders() {
        int remindersSent = 0;

        for (int days : REMINDER_WINDOWS_DAYS) {
            LocalDate targetDate = LocalDate.now().plusDays(days);
            List<VehicleDocument> documents = vehicleDocumentRepository.findByExpiryDateBetween(targetDate, targetDate);

            for (VehicleDocument document : documents) {
                if (alreadySentToday(document)) {
                    continue;
                }
                if (sendReminder(document, days)) {
                    remindersSent++;
                }
            }
        }

        return remindersSent;
    }

    private boolean alreadySentToday(VehicleDocument document) {
        return document.getReminderSentAt() != null
                && document.getReminderSentAt().toLocalDate().equals(LocalDate.now());
    }

    private boolean sendReminder(VehicleDocument document, int days) {
        if (adminEmail == null || adminEmail.isBlank()) {
            log.warn("ADMIN_EMAIL not set — skipping expiry reminder for document {}", document.getId());
            return false;
        }

        String vehicleNo = document.getVehicle() != null ? document.getVehicle().getVehicleNo() : "Unknown";
        EmailContent content = emailTemplateService.documentExpiryTemplate(
                vehicleNo, document.getDocumentType(), document.getDocumentNo(), document.getExpiryDate(), days);

        notificationService.sendEmail(adminEmail, content.subject(), content.body());

        document.setReminderSentAt(LocalDateTime.now());
        vehicleDocumentRepository.save(document);
        return true;
    }
}
