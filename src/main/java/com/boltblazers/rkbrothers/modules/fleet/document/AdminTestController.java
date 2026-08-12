package com.boltblazers.rkbrothers.modules.fleet.document;

import com.boltblazers.rkbrothers.core.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Dev/test-only: lets an admin manually fire the document expiry scheduler
 * without waiting for its 8 AM cron. Gated to non-prod profiles as an extra
 * safeguard on top of admin-only access — remove before production.
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Profile("!prod")
public class AdminTestController {

    private final DocumentExpirySchedulerService documentExpirySchedulerService;

    @PostMapping("/trigger-expiry-check")
    public ApiResponse<Map<String, Integer>> triggerExpiryCheck() {
        int remindersSent = documentExpirySchedulerService.checkAndSendReminders();
        return ApiResponse.success(Map.of("remindersSent", remindersSent));
    }
}
