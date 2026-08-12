package com.boltblazers.rkbrothers.core.config;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.output.MigrateResult;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Central place to customize Flyway behavior as the ERP grows additional
 * modules (Wages, HR, Statutory Compliance) that may ship their own
 * classpath:db/migration locations under the same DB.
 */
@Slf4j
@Configuration
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            MigrateResult result = flyway.migrate();
            log.info("Flyway applied {} migration(s), schema version now {}",
                    result.migrationsExecuted, result.targetSchemaVersion);
        };
    }
}
