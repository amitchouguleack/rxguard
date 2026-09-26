# Copyright (c) 2026 Amit Chougule. All rights reserved.
# syntax=docker/dockerfile:1
FROM eclipse-temurin:25-jdk-noble AS build
WORKDIR /src
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -ntp dependency:go-offline
COPY src src
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -ntp -DskipTests package spotless:check \
 && java -Djarmode=tools -jar target/rx-gateway-*.jar extract --layers --launcher --destination /extracted

FROM eclipse-temurin:25-jre-noble
RUN useradd --system --uid 10001 app
WORKDIR /app
# Layers ordered from least to most frequently changed, for smaller pushes and pulls.
COPY --from=build /extracted/dependencies/ ./
COPY --from=build /extracted/spring-boot-loader/ ./
COPY --from=build /extracted/snapshot-dependencies/ ./
COPY --from=build /extracted/application/ ./
ARG GIT_SHA=dev
ENV RXGUARD_GIT_SHA=${GIT_SHA}
# Tuned for Render free (512 MB, 0.1 CPU). See docs/decisions/0006-jvm-memory-512mb.md.
ENV JAVA_TOOL_OPTIONS="-XX:+UseSerialGC -XX:MaxRAMPercentage=50 -XX:MaxMetaspaceSize=128m \
-XX:ReservedCodeCacheSize=48m -Xss512k -XX:TieredStopAtLevel=1 -XX:+ExitOnOutOfMemoryError"
LABEL org.opencontainers.image.source="https://github.com/amitchouguleack/rxguard" \
      org.opencontainers.image.description="RxGuard rx-gateway (synthetic data only)"
USER app
EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
