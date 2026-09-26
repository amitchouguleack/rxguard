/* Copyright (c) 2026 Amit Chougule. All rights reserved. */
package dev.rxguard.gateway.version;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Reports the build the running image came from, so CD can prove a deploy went live. */
@RestController
public class VersionController {

  private final String version;
  private final String gitSha;

  public VersionController(
      @Value("${rxguard.version}") String version, @Value("${rxguard.git-sha}") String gitSha) {
    this.version = version;
    this.gitSha = gitSha;
  }

  @GetMapping("/api/version")
  public Map<String, String> version() {
    return Map.of("service", "rx-gateway", "version", version, "gitSha", gitSha);
  }
}
