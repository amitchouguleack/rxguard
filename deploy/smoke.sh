#!/usr/bin/env bash
# Copyright (c) 2026 Amit Chougule. All rights reserved.
# Smoke test for a deployed rx-gateway. Waits for the cold start and for the expected build to be
# live, then checks health. Usage: deploy/smoke.sh <base-url> <expected-git-sha> [timeout-seconds]
set -euo pipefail

BASE_URL="${1:?base URL required}"
EXPECTED_SHA="${2:?expected git SHA required}"
TIMEOUT="${3:-900}"
BASE_URL="${BASE_URL%/}"

json_field() { python3 -c "import json,sys; print(json.load(sys.stdin).get('$1', ''))" 2>/dev/null; }

echo "Waiting up to ${TIMEOUT}s for ${BASE_URL} to serve ${EXPECTED_SHA}"
start=$(date +%s)
live_sha=""
while true; do
  elapsed=$(( $(date +%s) - start ))
  live_sha=$(curl -sf --max-time 30 "${BASE_URL}/api/version" | json_field gitSha || true)
  if [ "${live_sha}" = "${EXPECTED_SHA}" ]; then
    echo "Build ${EXPECTED_SHA} is live after ${elapsed}s"
    break
  fi
  if [ "${elapsed}" -ge "${TIMEOUT}" ]; then
    echo "FAIL: after ${elapsed}s the live build is '${live_sha:-unreachable}', expected ${EXPECTED_SHA}"
    exit 1
  fi
  echo "  ${elapsed}s: live build '${live_sha:-unreachable}', retrying in 15s"
  sleep 15
done

check() {
  local path="$1" field="$2" want="$3" got
  got=$(curl -sf --max-time 30 "${BASE_URL}${path}" | json_field "${field}" || true)
  if [ "${got}" != "${want}" ]; then
    echo "FAIL: ${path} ${field}='${got}', expected '${want}'"
    exit 1
  fi
  echo "OK: ${path} ${field}=${got}"
}

check /health status UP
check /health service rx-gateway
# Aggregate actuator health includes the database connection.
check /actuator/health status UP
echo "Smoke test passed"
