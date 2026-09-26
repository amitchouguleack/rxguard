/* Copyright (c) 2026 Amit Chougule. All rights reserved. */
package dev.rxguard.gateway.version;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.rxguard.gateway.config.WebCorsConfig;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(VersionController.class)
@Import(WebCorsConfig.class)
@TestPropertySource(
    properties = {"rxguard.git-sha=0123abc", "rxguard.cors.allowed-origins=https://pages.example"})
class VersionControllerTest {

  @Autowired private MockMvc mvc;

  @Test
  @Tag("REQ-OPS-002")
  void reportsGitShaAndVersion() throws Exception {
    mvc.perform(get("/api/version"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.service").value("rx-gateway"))
        .andExpect(jsonPath("$.gitSha").value("0123abc"))
        // Resource filtering must replace the @project.version@ placeholder.
        .andExpect(jsonPath("$.version").value(matchesPattern("\\d+\\.\\d+\\.\\d+.*")));
  }

  @Test
  @Tag("REQ-UI-006")
  void allowsConfiguredWebOrigin() throws Exception {
    mvc.perform(get("/api/version").header("Origin", "https://pages.example"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", "https://pages.example"));
  }

  @Test
  @Tag("REQ-UI-006")
  void rejectsOtherOrigins() throws Exception {
    mvc.perform(get("/api/version").header("Origin", "https://evil.example"))
        .andExpect(status().isForbidden());
  }
}
