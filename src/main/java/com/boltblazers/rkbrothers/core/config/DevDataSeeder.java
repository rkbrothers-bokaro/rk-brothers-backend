package com.boltblazers.rkbrothers.core.config;

import com.boltblazers.rkbrothers.core.auth.Role;
import com.boltblazers.rkbrothers.core.auth.User;
import com.boltblazers.rkbrothers.core.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds default logins for local development so the API is usable
 * immediately against the in-memory H2 database, with no manual setup.
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataSeeder implements CommandLineRunner {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PHONE = "9999999999";
    private static final String ADMIN_PASSWORD = "admin123";

    private static final String STAFF_USERNAME = "staff";
    private static final String STAFF_PHONE = "8888888888";
    private static final String STAFF_PASSWORD = "staff123";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUser(ADMIN_USERNAME, ADMIN_PHONE, ADMIN_PASSWORD, "admin@rkbrothers.local", "Default Admin", Role.ADMIN);
        seedUser(STAFF_USERNAME, STAFF_PHONE, STAFF_PASSWORD, "staff@rkbrothers.local", "Default Staff", Role.STAFF);
    }

    private void seedUser(String username, String phone, String rawPassword, String email, String fullName, Role role) {
        if (userRepository.existsByPhone(phone)) {
            return;
        }

        User user = User.builder()
                .username(username)
                .phone(phone)
                .password(passwordEncoder.encode(rawPassword))
                .email(email)
                .fullName(fullName)
                .role(role)
                .enabled(true)
                .build();

        userRepository.save(user);
        log.info("Seeded default dev {} user -> phone: '{}', password: '{}'", role.name().toLowerCase(), phone, rawPassword);
    }
}
