/* Copyright (c) 2026 Amit Chougule. All rights reserved. */
package dev.rxguard.gateway.health;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Liveness endpoint polled by the web UI's "Waking up services…" panel and by smoke tests. */
@RestController
public class HealthController {

  static final String SERVICE_NAME = "rx-gateway";

  @GetMapping("/health")
  public Map<String, String> health() {
    return Map.of("status", "UP", "service", SERVICE_NAME);
  }

  @GetMapping("/")
  public Map<String, String> hello() {
    return Map.of(
        "message", "Hello from " + SERVICE_NAME, "notice", "SYNTHETIC DATA — NOT FOR CLINICAL USE");
  }
}
