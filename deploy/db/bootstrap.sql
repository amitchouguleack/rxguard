-- Copyright (c) 2026 Amit Chougule. All rights reserved.
--
-- One-time database bootstrap for rx-gateway. Run as an admin role once per environment:
--   local: runs automatically from the compose db container (deploy/db/init.sh)
--   Neon:  psql "<owner connection string>" -v gateway_owner_password=... \
--            -v gateway_app_password=... -f deploy/db/bootstrap.sql
--
-- Roles (least privilege, REQ-SEC-004):
--   gateway_owner  owns the gateway schema; used only by Flyway migrations (DDL)
--   gateway_app    used by the running service; data access only, granted by migration V1
\set ON_ERROR_STOP on

CREATE ROLE gateway_owner LOGIN PASSWORD :'gateway_owner_password';
CREATE ROLE gateway_app LOGIN PASSWORD :'gateway_app_password';

-- Flyway (as gateway_owner) creates the gateway schema, so the owner needs CREATE on this database.
GRANT CREATE ON DATABASE :"DBNAME" TO gateway_owner;

ALTER ROLE gateway_owner SET search_path = gateway;
ALTER ROLE gateway_app SET search_path = gateway;
