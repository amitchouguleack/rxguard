/* Copyright (c) 2026 Amit Chougule. All rights reserved. */
package dev.rxguard.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Lets the static web UI (GitHub Pages) read the public status endpoints, and nothing else. */
@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

  private final String[] allowedOrigins;

  public WebCorsConfig(@Value("${rxguard.cors.allowed-origins}") String[] allowedOrigins) {
    this.allowedOrigins = allowedOrigins;
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    for (String path : new String[] {"/health", "/api/version"}) {
      registry.addMapping(path).allowedOrigins(allowedOrigins).allowedMethods("GET");
    }
  }
}
