-- Copyright (c) 2026 Amit Chougule. All rights reserved.
-- Runs as gateway_owner. The runtime role gets data access to tables created from now on,
-- but no DDL. Flyway's own history table predates these defaults, so gateway_app cannot read it.
REVOKE ALL ON SCHEMA gateway FROM PUBLIC;
GRANT USAGE ON SCHEMA gateway TO gateway_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA gateway
  GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO gateway_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA gateway
  GRANT USAGE, SELECT ON SEQUENCES TO gateway_app;
