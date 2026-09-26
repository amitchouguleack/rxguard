/* Copyright (c) 2026 Amit Chougule. All rights reserved. */
package dev.rxguard.gateway;

import dev.rxguard.gateway.support.PostgresTestDatabase;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
class RxGatewayApplicationTest {

  @DynamicPropertySource
  static void database(DynamicPropertyRegistry registry) {
    PostgresTestDatabase.register(registry);
  }

  @Test
  @Tag("REQ-OPS-001")
  void contextLoads() {}
}
