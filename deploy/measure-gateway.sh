#!/usr/bin/env bash
# Copyright (c) 2026 Amit Chougule. All rights reserved.
# Measures rx-gateway cold start and memory under Render free-plan limits (0.1 CPU, 512 MB).
# Usage: docker compose --profile db up -d && docker compose build rx-gateway
#        deploy/measure-gateway.sh              # tuned JVM flags from the Dockerfile
#        deploy/measure-gateway.sh --defaults   # plain JVM defaults, for comparison
set -euo pipefail
NAME=rxg-measure
EXTRA=()
[ "${1:-}" = "--defaults" ] && EXTRA=(-e JAVA_TOOL_OPTIONS=)

docker rm -f "$NAME" >/dev/null 2>&1 || true
# Fresh schema so every run includes the Flyway migration.
docker compose exec -T db psql -U "${POSTGRES_USER:-rxguard}" -d "${POSTGRES_DB:-rxguard}" \
  -qc 'DROP SCHEMA IF EXISTS gateway CASCADE' >/dev/null

start=$(date +%s)
docker run -d --name "$NAME" --network rxguard_default --cpus=0.1 --memory=512m -p 18080:8080 \
  "${EXTRA[@]}" \
  -e DB_URL="jdbc:postgresql://db:5432/${POSTGRES_DB:-rxguard}" \
  -e DB_APP_USER=gateway_app -e DB_APP_PASSWORD="${GATEWAY_APP_PASSWORD:-gateway-app-local-dev}" \
  -e DB_MIGRATION_USER=gateway_owner \
  -e DB_MIGRATION_PASSWORD="${GATEWAY_OWNER_PASSWORD:-gateway-owner-local-dev}" \
  rxguard-rx-gateway >/dev/null

until curl -sf localhost:18080/health >/dev/null; do
  sleep 1
  if [ $(( $(date +%s) - start )) -gt 600 ]; then echo "no /health after 600s"; docker logs "$NAME" | tail -20; exit 1; fi
done
echo "first /health after: $(( $(date +%s) - start ))s"
docker logs "$NAME" 2>&1 | grep -oE 'Started RxGatewayApplication in .*' || true
docker stats --no-stream --format 'memory after start: {{.MemUsage}}' "$NAME"
for _ in $(seq 1 200); do
  curl -s localhost:18080/api/version >/dev/null
  curl -s localhost:18080/actuator/health >/dev/null
done
docker stats --no-stream --format 'memory after 400 requests: {{.MemUsage}}' "$NAME"
docker rm -f "$NAME" >/dev/null
