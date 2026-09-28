# Performance

Only real measurements, each with the command to reproduce it.

## rx-gateway cold start under free-plan limits

Environment: GitHub Codespace (2 cores, 8 GB). The container was limited to `--cpus=0.1 --memory=512m` to match Render's free plan, and used the local compose Postgres 17. Dates and full context are in [ADR 0006](decisions/0006-jvm-memory-512mb.md).

| JVM flags | First `/health` | Memory after 400 requests |
|---|---|---|
| Tuned (Dockerfile defaults) | 56–61 s (2 runs) | 126–128 MiB |
| JVM defaults | 120 s (1 run) | 182 MiB |

```bash
docker compose --profile db up -d && docker compose build rx-gateway
deploy/measure-gateway.sh [--defaults]
```

## rx-gateway on Render (live)

Measured against the live free-tier service after it had been idle for 18 minutes (asleep), on 2026-09-28. This is one sample, taken from the Codespace, so the time includes network latency.

| Request | Time |
|---|---|
| First `GET /health` (Render wakes the service) | 52.4 s |
| Next `GET /api/version` (warm) | 0.14 s |
| `GET /actuator/health` (includes the database; Neon may also have been asleep) | 1.0 s |

```bash
# after at least 15 minutes with no traffic:
curl -s -o /dev/null -w "%{time_total}s\n" https://rxguard-rx-gateway.onrender.com/health
```

Deploy speed: on the first automatic CD run (run `36401050163`), the new build was serving 45 s after the smoke test started.
