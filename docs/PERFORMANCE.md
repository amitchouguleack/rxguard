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

The live Render cold-start time will be added after the first deploy.
