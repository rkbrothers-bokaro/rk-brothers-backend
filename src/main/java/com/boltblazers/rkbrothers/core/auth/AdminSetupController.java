package com.boltblazers.rkbrothers.core.auth;

import com.boltblazers.rkbrothers.core.auth.dto.AdminSetupRequest;
import com.boltblazers.rkbrothers.core.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * One-time bootstrap endpoint for standing up the first admin user when a
 * fresh database has no seed data yet. Disabled by default (fails closed
 * when ADMIN_SETUP_KEY is unset) and refuses to run once any user exists.
 *
 * Remove this controller (or unset ADMIN_SETUP_KEY) once setup is confirmed
 * done -- it is not meant to stay reachable long-term.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminSetupController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin-setup-key:}")
    private String configuredSetupKey;

    @PostMapping("/setup")
    public ResponseEntity<ApiResponse<Void>> setup(@Valid @RequestBody AdminSetupRequest request) {
        if (!isValidKey(request.setupKey())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Setup not available"));
        }

        if (userRepository.count() > 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error("Setup already completed"));
        }

        User admin = User.builder()
                .username("admin")
                .phone("9999999999")
                .password(passwordEncoder.encode("admin123"))
                .email("admin@rkbrothers.local")
                .fullName("Default Admin")
                .role(Role.ADMIN)
                .enabled(true)
                .build();
        userRepository.save(admin);

        log.warn("Bootstrap admin user created via /api/v1/admin/setup. Disable ADMIN_SETUP_KEY now.");
        return ResponseEntity.ok(ApiResponse.success("Admin created", null));
    }

    private boolean isValidKey(String suppliedKey) {
        if (configuredSetupKey == null || configuredSetupKey.isBlank() || suppliedKey == null) {
            return false;
        }
        byte[] configured = configuredSetupKey.getBytes(StandardCharsets.UTF_8);
        byte[] supplied = suppliedKey.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(configured, supplied);
    }
}
