# ADR 0001: Java 25 LTS with Spring Boot 4.1

- **Status:** Accepted
- **Date:** 2026-09-25

## Context
rx-gateway is the Java service. It will call the C++ interaction engine in-process through the **Foreign Function & Memory (FFM) API**. It will also host Spring Authorization Server, HAPI FHIR and HAPI HL7v2. The build plan prefers Java 25 LTS, with a fallback to Java 21 plus FFM in preview if Spring Boot does not officially support 25.

What was verified on 2026-09-25:
- The Spring Boot 4.1 system requirements page (<https://docs.spring.io/spring-boot/4.1/system-requirements.html>) says: *"Spring Boot 4.1.1 requires at least Java 17 and is compatible with versions up to and including Java 26."*
- Maven Central lists `4.1.1` as the newest GA release. (`4.2.0-M2` is a milestone and is not used.)
- FFM has been a final, non-preview API since Java 22 (JEP 454), so no `--enable-preview` flag is needed on Java 25.
- The Codespace ships `openjdk 25.0.4.1 2026-08-18 LTS`.

## Decision
- Use **Java 25 LTS** (Eclipse Temurin) with **Spring Boot 4.1.x** (the newest GA line).
- Build with the Maven Wrapper so CI and every developer machine use the same Maven version.
- Use FFM as a final API. Enable native access only for the module or package that loads the engine (`--enable-native-access`).

## Consequences
- No preview flags, so no preview-API churn when moving to a later JDK.
- Spring Boot 4 is built on Spring Framework 7 and Jakarta EE 11. Third-party libraries (HAPI FHIR, HAPI HL7v2, Spring Authorization Server) must be checked for compatibility when each is added. If one is incompatible, a follow-up ADR records the workaround.
- Java 25 container images are larger than a minimal runtime. Later phases will use a JRE-only base image and tune memory for the 512 MB Render limit (see ADR 0003).
