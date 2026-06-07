package com.camplink;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * One-off schema corrections that Hibernate's {@code ddl-auto=update} cannot
 * perform itself.
 *
 * <p>Hibernate generates a CHECK constraint for {@code @Enumerated(STRING)}
 * columns listing the enum values known at table-creation time, and never
 * updates it when new values are added. The original {@code users.role} check
 * only permitted BUYER/SELLER/ADMIN, so once RIDER and DRIVER were introduced
 * any attempt to save such a user failed with a constraint violation. We drop
 * the stale constraint; the enum is still enforced in the application layer.</p>
 *
 * <p>Runs after the schema has been created/updated. Idempotent and safe on a
 * fresh database (the constraint simply may not exist).</p>
 */
@Component
@Order(0)
@RequiredArgsConstructor
@Slf4j
public class SchemaFixup implements ApplicationRunner {

    private final JdbcTemplate jdbc;

    @Override
    public void run(ApplicationArguments args) {
        // Enum columns whose value set has grown since the table was created.
        dropConstraint("users", "users_role_check");
        dropConstraint("notifications", "notifications_type_check");
    }

    private void dropConstraint(String table, String constraint) {
        try {
            jdbc.execute("ALTER TABLE " + table + " DROP CONSTRAINT IF EXISTS " + constraint);
            log.info("Schema fixup: ensured {} on {} is removed", constraint, table);
        } catch (Exception e) {
            log.warn("Schema fixup: could not drop {} on {}: {}", constraint, table, e.getMessage());
        }
    }
}
