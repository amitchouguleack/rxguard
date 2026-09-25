# Copyright (c) 2026 Amit Chougule. All rights reserved.
# syntax=docker/dockerfile:1
FROM eclipse-temurin:25-jdk-noble AS build
WORKDIR /src
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -ntp dependency:go-offline
COPY src src
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -ntp -DskipTests package spotless:check

FROM eclipse-temurin:25-jre-noble
RUN useradd --system --uid 10001 app
WORKDIR /app
COPY --from=build /src/target/rx-gateway-*.jar app.jar
USER app
EXPOSE 8080
# JVM memory tuning for the 512 MB Render limit happens in Phase 1.
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
