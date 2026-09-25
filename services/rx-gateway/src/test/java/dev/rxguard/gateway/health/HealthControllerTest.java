/* Copyright (c) 2026 Amit Chougule. All rights reserved. */
package dev.rxguard.gateway.health;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HealthController.class)
class HealthControllerTest {

  @Autowired private MockMvc mvc;

  @Test
  @Tag("REQ-OPS-001")
  void healthReportsUpWithServiceName() throws Exception {
    mvc.perform(get("/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"))
        .andExpect(jsonPath("$.service").value("rx-gateway"));
  }

  @Test
  @Tag("REQ-UI-001")
  void rootResponseCarriesSyntheticDataNotice() throws Exception {
    mvc.perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.notice").value("SYNTHETIC DATA — NOT FOR CLINICAL USE"));
  }
}
