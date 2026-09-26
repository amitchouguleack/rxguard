#!/usr/bin/env bash
# Copyright (c) 2026 Amit Chougule. All rights reserved.
# Runs once when the local compose Postgres volume is first created.
set -euo pipefail
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" \
  -v gateway_owner_password="$GATEWAY_OWNER_PASSWORD" \
  -v gateway_app_password="$GATEWAY_APP_PASSWORD" \
  -f /bootstrap/bootstrap.sql
