/* Copyright (c) 2026 Amit Chougule. All rights reserved. */
package dev.rxguard.gateway.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.rxguard.gateway.support.PostgresTestDatabase;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/** The runtime role can use data in its own schema and nothing more (REQ-SEC-004). */
@SpringBootTest
@Tag("REQ-SEC-004")
class DatabaseLeastPrivilegeTest {

  private static final String PERMISSION_DENIED = "42501";

  @Autowired private DataSource appDataSource;

  @DynamicPropertySource
  static void database(DynamicPropertyRegistry registry) {
    PostgresTestDatabase.register(registry);
  }

  /** Simulates another service's schema that rx-gateway must never read. */
  @BeforeAll
  static void createForeignSchema() throws SQLException {
    try (Connection admin = connect("test", "test");
        Statement st = admin.createStatement()) {
      st.execute("CREATE ROLE pharmacy_owner_probe NOLOGIN");
      st.execute("CREATE SCHEMA pharmacy_probe AUTHORIZATION pharmacy_owner_probe");
      st.execute("CREATE TABLE pharmacy_probe.rx (id int)");
      st.execute("ALTER TABLE pharmacy_probe.rx OWNER TO pharmacy_owner_probe");
    }
  }

  @Test
  void flywayMigratedAsOwner() throws SQLException {
    try (Connection owner = connect("gateway_owner", PostgresTestDatabase.OWNER_PASSWORD);
        Statement st = owner.createStatement();
        var rs =
            st.executeQuery(
                "SELECT count(*) FROM gateway.flyway_schema_history WHERE success AND version = '1'")) {
      rs.next();
      assertThat(rs.getInt(1)).isEqualTo(1);
    }
  }

  @Test
  void appRunsAsRuntimeRole() {
    String user =
        new JdbcTemplate(appDataSource).queryForObject("SELECT current_user", String.class);
    assertThat(user).isEqualTo("gateway_app");
  }

  @Test
  void runtimeRoleCannotRunDdl() {
    JdbcTemplate app = new JdbcTemplate(appDataSource);
    assertDenied(() -> app.execute("CREATE TABLE gateway.sneaky (id int)"));
    assertDenied(() -> app.execute("CREATE TABLE public.sneaky (id int)"));
  }

  @Test
  void runtimeRoleGetsDataAccessToOwnerTablesButCannotAlterThem() throws SQLException {
    try (Connection owner = connect("gateway_owner", PostgresTestDatabase.OWNER_PASSWORD);
        Statement st = owner.createStatement()) {
      st.execute("CREATE TABLE gateway.probe (id serial PRIMARY KEY, note text)");
    }
    JdbcTemplate app = new JdbcTemplate(appDataSource);
    app.update("INSERT INTO gateway.probe (note) VALUES ('synthetic')");
    app.update("UPDATE gateway.probe SET note = 'changed'");
    assertThat(app.queryForObject("SELECT note FROM gateway.probe", String.class))
        .isEqualTo("changed");
    app.update("DELETE FROM gateway.probe");

    assertDenied(() -> app.execute("DROP TABLE gateway.probe"));
    assertDenied(() -> app.execute("ALTER TABLE gateway.probe ADD COLUMN x int"));
  }

  @Test
  void runtimeRoleCannotReadMigrationHistory() {
    JdbcTemplate app = new JdbcTemplate(appDataSource);
    assertDenied(() -> app.queryForList("SELECT * FROM gateway.flyway_schema_history"));
  }

  @Test
  void runtimeRoleCannotReadAnotherServicesSchema() {
    JdbcTemplate app = new JdbcTemplate(appDataSource);
    assertDenied(() -> app.queryForList("SELECT * FROM pharmacy_probe.rx"));
  }

  private static Connection connect(String user, String password) throws SQLException {
    return DriverManager.getConnection(PostgresTestDatabase.POSTGRES.getJdbcUrl(), user, password);
  }

  private static void assertDenied(Runnable action) {
    assertThatThrownBy(action::run)
        .rootCause()
        .isInstanceOf(SQLException.class)
        .extracting(e -> ((SQLException) e).getSQLState())
        .isEqualTo(PERMISSION_DENIED);
  }
}
