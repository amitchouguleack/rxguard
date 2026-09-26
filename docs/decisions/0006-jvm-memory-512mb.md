# ADR 0006: JVM settings for Render's 512 MB / 0.1 CPU free instance

- **Status:** Accepted
- **Date:** 2026-09-26

## Context
Render's free web service gives 512 MB of RAM and 0.1 CPU ([ADR 0003](0003-free-hosting.md)). A JVM left to its defaults sizes itself for throughput on a big machine, not for a fast cold start on a tenth of a core. Every visit after 15 idle minutes pays the cold start, so startup time matters more than peak throughput for this demo.

## Decision
The image sets these flags through `JAVA_TOOL_OPTIONS`:

| Flag | Why |
|---|---|
| `-XX:+UseSerialGC` | Lowest memory overhead and no GC threads competing for 0.1 CPU. |
| `-XX:MaxRAMPercentage=50` | Heap up to 256 MB, leaving room for metaspace, code cache, thread stacks and native memory. |
| `-XX:MaxMetaspaceSize=128m`, `-XX:ReservedCodeCacheSize=48m`, `-Xss512k` | Cap the non-heap areas so the total stays well under 512 MB. |
| `-XX:TieredStopAtLevel=1` | C1 compiler only. Much less JIT work during startup, at the cost of peak throughput. |
| `-XX:+ExitOnOutOfMemoryError` | Crash and let Render restart the instance instead of limping along. |

The image also uses Spring Boot's layered jar extraction, so dependency layers are cached between builds and pushes.

## Measurements
Measured in the Codespace with the container limited to Render's free plan (`--cpus=0.1 --memory=512m`), against the local compose Postgres, including the Flyway migration. Reproduce with:

```bash
docker compose --profile db up -d && docker compose build rx-gateway
deploy/measure-gateway.sh              # tuned flags
deploy/measure-gateway.sh --defaults   # JVM defaults
```

| Run (2026-09-26) | First `/health` | Spring "Started in" | Memory after start | Memory after 400 requests |
|---|---|---|---|---|
| Tuned, run 1 | 61 s | 50.5 s | 122.1 MiB | 127.8 MiB |
| Tuned, run 2 (script) | 56 s | 46.7 s | 120.5 MiB | 126.4 MiB |
| JVM defaults (1 run) | 120 s | 104.6 s | 160.6 MiB | 181.8 MiB |

These are few runs on a shared Codespace, so they show a direction, not a precise figure. Render's real cold start also includes pulling the image and routing, and will be measured separately after the first live deploy.

## Consequences
- Cold start roughly halves, and memory stays around a quarter of the limit, which leaves headroom for HAPI FHIR, HL7v2 and the authorization server. The ADR 0003 rule still applies: measure again when those land, and split the service if it goes over about 450 MB.
- C1-only code is slower under sustained load. That's the right trade for a mostly idle demo. Phase 12 load tests should run with and without the flag and report both.
- The flags are an environment variable, so Render can override them without a rebuild.
