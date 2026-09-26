/* Copyright (c) 2026 Amit Chougule. All rights reserved. */
package dev.rxguard.gateway.support;

import java.nio.file.Path;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

/**
 * One PostgreSQL container shared by all integration tests. It is bootstrapped with the real {@code
 * deploy/db/bootstrap.sql} through psql, exactly as on Neon, so the tests exercise the production
 * role setup rather than a superuser.
 */
public final class PostgresTestDatabase {

  public static final String OWNER_PASSWORD = "test-owner-password";
  public static final String APP_PASSWORD = "test-app-password";

  private static final Path BOOTSTRAP = Path.of("../../deploy/db/bootstrap.sql");

  public static final PostgreSQLContainer POSTGRES =
      new PostgreSQLContainer("postgres:17-alpine")
          .withCopyFileToContainer(
              MountableFile.forHostPath(BOOTSTRAP), "/bootstrap/bootstrap.sql");

  static {
    POSTGRES.start();
    try {
      ExecResult result =
          POSTGRES.execInContainer(
              "psql",
              "-U",
              POSTGRES.getUsername(),
              "-d",
              POSTGRES.getDatabaseName(),
              "-v",
              "gateway_owner_password=" + OWNER_PASSWORD,
              "-v",
              "gateway_app_password=" + APP_PASSWORD,
              "-f",
              "/bootstrap/bootstrap.sql");
      if (result.getExitCode() != 0) {
        throw new IllegalStateException("bootstrap.sql failed: " + result.getStderr());
      }
    } catch (Exception e) {
      throw new IllegalStateException("Could not bootstrap test database", e);
    }
  }

  private PostgresTestDatabase() {}

  /** Points the app at the container using the same environment variables as production. */
  public static void register(DynamicPropertyRegistry registry) {
    registry.add("DB_URL", POSTGRES::getJdbcUrl);
    registry.add("DB_APP_USER", () -> "gateway_app");
    registry.add("DB_APP_PASSWORD", () -> APP_PASSWORD);
    registry.add("DB_MIGRATION_USER", () -> "gateway_owner");
    registry.add("DB_MIGRATION_PASSWORD", () -> OWNER_PASSWORD);
  }
}
