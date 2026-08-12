package com.boltblazers.rkbrothers.core.notification;

import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class EmailTemplateService {

    public record EmailContent(String subject, String body) {
    }

    public EmailContent documentExpiryTemplate(String vehicleNo, String documentType, String documentNo,
                                                LocalDate expiryDate, int daysLeft) {
        String subject = "⚠️ Document Expiry Alert — %s %s expires in %d days"
                .formatted(vehicleNo, documentType, daysLeft);

        String body = """
                Dear Admin,

                This is an automated reminder from RK Brothers Fleet Management System.

                DOCUMENT EXPIRY ALERT
                ─────────────
                Vehicle:       %1$s
                Document Type: %2$s
                Document No:   %3$s
                Expiry Date:   %4$s
                Days Remaining: %5$d

                Please renew this document before it expires to avoid any operational disruption.

                ─────────────
                वाहन:          %1$s
                दस्तावेज़ प्रकार: %2$s
                दस्तावेज़ संख्या: %3$s
                समाप्ति तिथि:   %4$s
                शेष दिन:       %5$d

                कृपया परिचालन व्यवधान से बचने के लिए समाप्ति से पहले इस दस्तावेज़ को नवीनीकृत करें।

                RK Brothers Fleet Management System""".formatted(vehicleNo, documentType, documentNo, expiryDate, daysLeft);

        return new EmailContent(subject, body);
    }
}
