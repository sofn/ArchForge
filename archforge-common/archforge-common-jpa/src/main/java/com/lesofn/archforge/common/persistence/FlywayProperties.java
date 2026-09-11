package com.lesofn.archforge.common.persistence;

import java.util.List;
import lombok.Data;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Flyway settings under the {@code arch-forge.flyway} prefix.
 *
 * <p>
 * Spring Boot 4 no longer ships a Flyway auto-configuration and flyway-core carries none, so
 * {@code spring.flyway.*} keys bind to nothing in this project — they were dead config. All
 * migration behaviour is owned by {@link FlywayConfig} and configured exclusively here.
 */
@Data
@ConfigurationProperties(prefix = "arch-forge.flyway")
public class FlywayProperties {

    /** Master switch. {@link FlywayConfig} is conditional on it; keep explicit per profile. */
    private boolean enabled = false;

    /** Classpath locations scanned for migrations. */
    private List<String> locations = List.of("classpath:db/migration");

    /** Default schema managed by Flyway (PostgreSQL: {@code public}). */
    private @Nullable String defaultSchema;

    /** Baseline a non-empty schema instead of failing on first migrate. */
    private boolean baselineOnMigrate = true;

    /** Version recorded for the baseline marker. */
    private String baselineVersion = "0";

    /** Character encoding of the SQL migrations. */
    private String encoding = "UTF-8";

    /** Validate applied migrations against the classpath before migrating. */
    private boolean validateOnMigrate = true;

    /** Allow migrations to be applied out of order. */
    private boolean outOfOrder = false;

    /**
     * Flyway {@code type:state} ignore patterns (e.g. {@code *:missing} tolerates migrations
     * recorded in an existing {@code flyway_schema_history} but deleted from the repository).
     */
    private List<String> ignoreMigrationPatterns = List.of();
}
