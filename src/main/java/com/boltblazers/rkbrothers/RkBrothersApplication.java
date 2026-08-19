package com.boltblazers.rkbrothers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class RkBrothersApplication {

    private static final Logger log = LoggerFactory.getLogger(RkBrothersApplication.class);

    public static void main(String[] args) {
        log.info("=== RK Brothers ERP Starting ===");
        log.info("DB_URL: {}", System.getenv("DB_URL"));
        log.info("DB_USERNAME: {}", System.getenv("DB_USERNAME"));
        log.info("SPRING_PROFILES_ACTIVE: {}", System.getenv("SPRING_PROFILES_ACTIVE"));
        SpringApplication.run(RkBrothersApplication.class, args);
    }
}
